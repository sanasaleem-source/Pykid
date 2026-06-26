package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.ProjectEntity
import com.example.database.ProjectRepository
import com.example.python.interpreter.PhoneModule
import com.example.python.interpreter.PythonInterpreter
import com.example.python.interpreter.TurtleState
import com.example.ui.components.TerminalLine
import com.example.ui.lessons.Lesson
import com.example.ui.lessons.LessonsData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PyKidViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository
    val allProjects: StateFlow<List<ProjectEntity>>

    // Device API Integrations
    val phoneModule = PhoneModule(application)
    val turtleState = TurtleState()

    // Editor & Navigation State
    var activeCode by mutableStateOf("")
    var currentProjectName by mutableStateOf("My Spell.py")
    var currentProject: ProjectEntity? by mutableStateOf(null)
    
    var activeLesson: Lesson? by mutableStateOf(null)
    var selectedTab by mutableStateOf(0) // 0 = Code & Console, 1 = Turtle Drawing, 2 = Academy, 3 = Files

    // Terminal Logging State
    var terminalLines = mutableListOf<TerminalLine>()
    var inputPrompt: String? by mutableStateOf(null)
    private var pendingInputDeferred: CompletableDeferred<String>? = null

    // Execution States
    var isRunning by mutableStateOf(false)
    private var executionJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load default code on startup
        val firstLesson = LessonsData.lessonsList.first()
        activeLesson = firstLesson
        activeCode = firstLesson.sampleCode
    }

    fun selectLesson(lesson: Lesson) {
        activeLesson = lesson
        activeCode = lesson.sampleCode
        selectedTab = 0 // Switch to editor
        clearTerminal()
        turtleState.reset()
    }

    fun selectProject(project: ProjectEntity) {
        currentProject = project
        currentProjectName = project.name
        activeCode = project.code
        activeLesson = null
        selectedTab = 0 // Switch to editor
        clearTerminal()
        turtleState.reset()
    }

    fun createNewProject(name: String) {
        val safeName = if (name.endsWith(".py")) name else "$name.py"
        viewModelScope.launch {
            val newProj = ProjectEntity(
                name = safeName,
                code = "# Draw, talk, or buzz! Type your script here...\nprint('My project is ready!')\nphone.speak('Hooray!')\n"
            )
            val newId = repository.insertProject(newProj)
            val savedProj = newProj.copy(id = newId.toInt())
            selectProject(savedProj)
        }
    }

    fun saveCurrentProject() {
        val projToSave = currentProject?.copy(
            code = activeCode,
            lastModified = System.currentTimeMillis()
        ) ?: ProjectEntity(
            name = currentProjectName,
            code = activeCode
        )

        viewModelScope.launch {
            val savedId = repository.insertProject(projToSave)
            if (currentProject == null) {
                currentProject = projToSave.copy(id = savedId.toInt())
            }
            phoneModule.toast("Spell Saved! 💾✨")
            phoneModule.playSound("success")
        }
    }

    fun deleteProject(id: Int) {
        viewModelScope.launch {
            repository.deleteProjectById(id)
            if (currentProject?.id == id) {
                currentProject = null
                currentProjectName = "Unsaved Spell.py"
                activeCode = "# Start typing here..."
            }
            phoneModule.toast("Project Deleted! 🗑️")
        }
    }

    fun runPythonCode() {
        if (isRunning) return
        
        clearTerminal()
        terminalLines.add(TerminalLine.Output(">>> Preparing Python sandbox engine..."))
        isRunning = true
        
        // Auto-switch to Turtle Canvas if code references turtle to show live drawings!
        if (activeCode.contains("turtle.")) {
            selectedTab = 1
        }

        executionJob = viewModelScope.launch {
            val interpreter = PythonInterpreter(
                onPrint = { text ->
                    terminalLines.add(TerminalLine.Output(text))
                },
                onInputRequired = { prompt, deferred ->
                    inputPrompt = prompt
                    pendingInputDeferred = deferred
                    // Force switch back to Editor/Terminal so child can type!
                    selectedTab = 0
                },
                phoneModule = phoneModule,
                turtleState = turtleState,
                onFinished = {
                    terminalLines.add(TerminalLine.Output("\n>>> Spell complete! 🌟✨"))
                    isRunning = false
                    inputPrompt = null
                },
                onError = { err, lineNum ->
                    terminalLines.add(TerminalLine.Error(err, lineNum))
                    phoneModule.playSound("error")
                    phoneModule.vibrate(100)
                    isRunning = false
                    inputPrompt = null
                }
            )

            interpreter.execute(activeCode)
        }
    }

    fun stopPythonCode() {
        if (!isRunning) return
        
        executionJob?.cancel()
        pendingInputDeferred?.cancel()
        inputPrompt = null
        isRunning = false
        terminalLines.add(TerminalLine.Output("\n>>> Spell stopped by user! 🛑"))
        phoneModule.playSound("error")
        phoneModule.vibrate(150)
    }

    fun sendTerminalInput(value: String) {
        val prompt = inputPrompt ?: "input"
        terminalLines.add(TerminalLine.InputText(prompt, value))
        inputPrompt = null
        pendingInputDeferred?.complete(value)
    }

    fun clearTerminal() {
        terminalLines.clear()
        inputPrompt = null
    }

    override fun onCleared() {
        super.onCleared()
        phoneModule.shutdown()
    }
}

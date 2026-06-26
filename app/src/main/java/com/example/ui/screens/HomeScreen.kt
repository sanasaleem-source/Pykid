package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.database.ProjectEntity
import com.example.ui.components.CodeEditor
import com.example.ui.components.TerminalView
import com.example.ui.components.TurtleCanvas
import com.example.ui.lessons.Lesson
import com.example.ui.lessons.LessonsData
import com.example.ui.lessons.ProjectIdea
import com.example.ui.viewmodel.PyKidViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PyKidViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.allProjects.collectAsState()
    var showNewProjDialog by remember { mutableStateOf(false) }
    var selectedLessonForSheet by remember { mutableStateOf<Lesson?>(null) }
    var selectedProjectForSheet by remember { mutableStateOf<ProjectIdea?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFD0BCFF), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (viewModel.selectedTab == 1) "🎨" else "🐍", 
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF381E72)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (viewModel.selectedTab) {
                                        0 -> if (viewModel.activeLesson != null) viewModel.activeLesson!!.title else viewModel.currentProjectName
                                        1 -> "Turtle Paint Canvas"
                                        2 -> "Magic Academy"
                                        else -> "My Spell Book"
                                    },
                                    color = Color(0xFF1D1B20),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when (viewModel.selectedTab) {
                                        0 -> if (viewModel.activeLesson != null) "Lesson Module 🎓" else "Custom Spell 🪄"
                                        1 -> "Watch turtle codes draw! 🖌️"
                                        2 -> "Learn Python Step-by-Step 📚"
                                        else -> "Manage your saved files 📂"
                                    },
                                    color = Color(0xFF49454F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    actions = {
                        if (viewModel.selectedTab == 0) {
                            // Quick New Spell Button
                            IconButton(
                                onClick = { showNewProjDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFE8DEF8), shape = CircleShape)
                                    .testTag("top_bar_new_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Spell",
                                    tint = Color(0xFF1D192B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            // Quick Save Button
                            IconButton(
                                onClick = { viewModel.saveCurrentProject() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFE8DEF8), shape = CircleShape)
                                    .testTag("top_bar_save_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Spell",
                                    tint = Color(0xFF1D192B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            // Play / Stop Button
                            IconButton(
                                onClick = {
                                    if (viewModel.isRunning) {
                                        viewModel.stopPythonCode()
                                    } else {
                                        viewModel.runPythonCode()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (viewModel.isRunning) Color(0xFFFEEBEE) else Color(0xFFE8F5E9), 
                                        shape = CircleShape
                                    )
                                    .testTag("top_bar_run_button")
                            ) {
                                Icon(
                                    imageVector = if (viewModel.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (viewModel.isRunning) "Stop" else "Run",
                                    tint = if (viewModel.isRunning) Color(0xFFC62828) else Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFEF7FF),
                        titleContentColor = Color(0xFF1D1B20)
                    )
                )
                // Fine light grey border-b matching Design HTML: border-b border-[#CAC4D0]/30
                HorizontalDivider(color = Color(0x33CAC4D0), thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0x33CAC4D0), thickness = 1.dp)
                NavigationBar(
                    containerColor = Color(0xFFF3EDF7),
                    tonalElevation = 0.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = viewModel.selectedTab == 0,
                        onClick = { viewModel.selectedTab = 0 },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Code") },
                        label = { Text("Code", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        ),
                        modifier = Modifier.testTag("tab_editor")
                    )
                    NavigationBarItem(
                        selected = viewModel.selectedTab == 1,
                        onClick = { viewModel.selectedTab = 1 },
                        icon = { Icon(Icons.Default.Palette, contentDescription = "Turtle") },
                        label = { Text("Canvas", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        ),
                        modifier = Modifier.testTag("tab_turtle")
                    )
                    NavigationBarItem(
                        selected = viewModel.selectedTab == 2,
                        onClick = { viewModel.selectedTab = 2 },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Academy") },
                        label = { Text("Lessons", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        ),
                        modifier = Modifier.testTag("tab_academy")
                    )
                    NavigationBarItem(
                        selected = viewModel.selectedTab == 3,
                        onClick = { viewModel.selectedTab = 3 },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Saved Spells") },
                        label = { Text("Spells", fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = Color(0xFFE8DEF8)
                        ),
                        modifier = Modifier.testTag("tab_spells")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (viewModel.selectedTab) {
                0 -> EditorTabScreen(viewModel)
                1 -> TurtleTabScreen(viewModel)
                2 -> AcademyTabScreen(
                    lessons = LessonsData.lessonsList,
                    projects = LessonsData.projectsList,
                    onLessonClick = { selectedLessonForSheet = it },
                    onProjectClick = { selectedProjectForSheet = it }
                )
                3 -> FilesTabScreen(
                    projects = projects,
                    currentProj = viewModel.currentProject,
                    onProjectSelect = { viewModel.selectProject(it) },
                    onProjectDelete = { viewModel.deleteProject(it.id) },
                    onCreateNewClick = { showNewProjDialog = true }
                )
            }
        }
    }

    // Modal Sheet or Dialog for Lesson Details
    if (selectedLessonForSheet != null) {
        val lesson = selectedLessonForSheet!!
        Dialog(onDismissRequest = { selectedLessonForSheet = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                    .testTag("lesson_dialog_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(lesson.icon, fontSize = 48.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.title,
                        color = Color(0xFF1D1B20),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = lesson.longDesc,
                        color = Color(0xFF49454F),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Code block review container (gorgeous High Density dark code box)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1B1F), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "MAGICAL SPELL:",
                            color = Color(0xFFD0BCFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lesson.sampleCode,
                            color = Color(0xFFE6E1E5),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Challenge box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "🎯 NEPH'S CHALLENGE:",
                                color = Color(0xFF6750A4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.challenge,
                                color = Color(0xFF1D1B20),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.selectLesson(lesson)
                            selectedLessonForSheet = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_lesson_button")
                    ) {
                        Text(
                            "LOAD SPELL TO WAND ⚡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet or Dialog for Project Idea Details
    if (selectedProjectForSheet != null) {
        val projIdea = selectedProjectForSheet!!
        Dialog(onDismissRequest = { selectedProjectForSheet = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                    .testTag("project_idea_dialog_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(projIdea.icon, fontSize = 48.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = projIdea.title,
                        color = Color(0xFF1D1B20),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = projIdea.desc,
                        color = Color(0xFF1D1B20),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = projIdea.goal,
                        color = Color(0xFF49454F),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Badged libraries used
                    Text(
                        "LIBRARIES USED:",
                        color = Color(0xFF6750A4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.Start),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        projIdea.libs.forEach { lib ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEADDFF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = lib,
                                    color = Color(0xFF21005D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Code block review container (gorgeous High Density dark code box)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1B1F), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "STARTER CODE SPELL:",
                            color = Color(0xFFD0BCFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = projIdea.starterCode,
                            color = Color(0xFFE6E1E5),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.activeCode = projIdea.starterCode
                            viewModel.currentProjectName = projIdea.title.replace("🧮", "").replace("🎮", "").replace("🎨", "").trim().replace(" ", "_") + ".py"
                            viewModel.activeLesson = null
                            viewModel.selectedTab = 0 // Code
                            viewModel.phoneModule.playSound("success")
                            viewModel.phoneModule.vibrate(150)
                            viewModel.clearTerminal()
                            viewModel.turtleState.reset()
                            selectedProjectForSheet = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_project_button")
                    ) {
                        Text(
                            "LOAD STARTER CODE ⚡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjDialog) {
        var newProjName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewProjDialog = false },
            title = { Text("Create New Spell 🪄", color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Give your Python script a playful name:", color = Color(0xFF49454F))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProjName,
                        onValueChange = { newProjName = it },
                        placeholder = { Text("my_drawing", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20),
                            focusedBorderColor = Color(0xFF6750A4),
                            unfocusedBorderColor = Color(0xFFCAC4D0),
                            focusedContainerColor = Color(0xFFF3EDF7),
                            unfocusedContainerColor = Color(0xFFF3EDF7)
                        ),
                        singleLine = true,
                        modifier = Modifier.testTag("new_project_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjName.isNotEmpty()) {
                            viewModel.createNewProject(newProjName)
                            showNewProjDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("new_project_confirm_button")
                ) {
                    Text("CREATE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjDialog = false }) {
                    Text("CANCEL", color = Color(0xFF6750A4))
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun EditorTabScreen(viewModel: PyKidViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        CodeEditor(
            code = viewModel.activeCode,
            onCodeChange = { viewModel.activeCode = it },
            fileName = if (viewModel.activeLesson != null) {
                "${viewModel.activeLesson?.title}.py"
            } else {
                viewModel.currentProjectName
            },
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        TerminalView(
            terminalLines = viewModel.terminalLines,
            inputPrompt = viewModel.inputPrompt,
            onSendInput = { viewModel.sendTerminalInput(it) },
            onClearTerminal = { viewModel.clearTerminal() },
            modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth()
        )
    }
}

@Composable
fun TurtleTabScreen(viewModel: PyKidViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        TurtleCanvas(
            turtleState = viewModel.turtleState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive control buttons for children
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { 
                    viewModel.turtleState.reset()
                    viewModel.phoneModule.playSound("success")
                    viewModel.phoneModule.vibrate(100)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF3EDF7), 
                    contentColor = Color(0xFF6750A4)
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Canvas", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("RESET CANVAS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { 
                    viewModel.clearTerminal()
                    viewModel.phoneModule.playSound("success")
                    viewModel.phoneModule.vibrate(100)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF3EDF7), 
                    contentColor = Color(0xFF6750A4)
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Terminal", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("CLEAR TERMINAL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Coordinates telemetry footer in clean High Density Card style
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TURTLE X, Y", color = Color(0xFF49454F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "(${viewModel.turtleState.x.toInt()}, ${viewModel.turtleState.y.toInt()})",
                        color = Color(0xFF6750A4),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFFCAC4D0))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ANGLE", color = Color(0xFF49454F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${(viewModel.turtleState.angle % 360).toInt()}°",
                        color = Color(0xFF6750A4),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFFCAC4D0))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PEN STATE", color = Color(0xFF49454F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (viewModel.turtleState.isPenDown) "PAINTING 🖌️" else "UP 🪶",
                        color = if (viewModel.turtleState.isPenDown) Color(0xFF2E7D32) else Color(0xFF49454F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AcademyTabScreen(
    lessons: List<Lesson>,
    projects: List<ProjectIdea>,
    onLessonClick: (Lesson) -> Unit,
    onProjectClick: (ProjectIdea) -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = Color(0xFFF3EDF7),
            contentColor = Color(0xFF6750A4),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Modules 🎓", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Projects 💡", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Setup 📱", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        when (selectedSubTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text(
                                text = "Python Magic Academy 🎓",
                                color = Color(0xFF1D1B20),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Run standard code interactively and play spells directly on your phone!",
                                color = Color(0xFF49454F),
                                fontSize = 13.sp
                            )
                        }
                    }

                    items(lessons) { lesson ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                                .clickable { onLessonClick(lesson) }
                                .testTag("lesson_card_${lesson.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFFF3EDF7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lesson.icon, fontSize = 26.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Module ${lesson.id}",
                                        color = Color(0xFF6750A4),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lesson.title,
                                        color = Color(0xFF1D1B20),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = lesson.shortDesc,
                                        color = Color(0xFF49454F),
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View Module",
                                    tint = Color(0xFF6750A4),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text(
                                text = "Creative Projects 💡",
                                color = Color(0xFF1D1B20),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Build fully working phone apps and games using pre-installed phone IDE libraries!",
                                color = Color(0xFF49454F),
                                fontSize = 13.sp
                            )
                        }
                    }

                    items(projects) { project ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                                .clickable { onProjectClick(project) }
                                .testTag("project_idea_card_${project.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFFEADDFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(project.icon, fontSize = 26.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Project ${project.id}",
                                        color = Color(0xFF6750A4),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = project.title,
                                        color = Color(0xFF1D1B20),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = project.desc,
                                        color = Color(0xFF49454F),
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "View Project",
                                    tint = Color(0xFF6750A4),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text(
                                text = "Set up Pydroid 3 📱",
                                color = Color(0xFF1D1B20),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Configure your nephew's phone for absolute offline, secure sandboxed coding!",
                                color = Color(0xFF49454F),
                                fontSize = 13.sp
                            )
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF6750A4), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Download & Sandbox Folder 📦", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1D1B20))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "• Open the Google Play Store on your Android phone.\n" +
                                           "• Search for 'Pydroid 3 - IDE for Python 3' and download it.\n" +
                                           "• Create a safe offline directory in internal storage called '/PyKidProjects' so all Python codes are kept perfectly organized and isolated.",
                                    color = Color(0xFF49454F),
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF6750A4), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Pre-Install Tech Libraries 🎮", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1D1B20))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "• Turtle (Graphics): Already built-in! No extra installation needed. Just write 'import turtle' and watch paintings appear!\n" +
                                           "• Pygame (Arcade Games): Go to Pydroid Menu -> PIP -> QuickInstall -> select Pygame (or PIP tab -> check 'Use prebuilt libraries' -> type 'pygame' -> tap Install).\n" +
                                           "• Kivy (GUI Interfaces): Go to Pydroid Menu -> PIP -> PIP tab -> check 'Use prebuilt libraries' -> type 'kivy' -> tap Install.",
                                    color = Color(0xFF49454F),
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF6750A4), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Safe Sandboxed Offline Mode 🔌", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF6750A4))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "• Pydroid 3 works completely offline without any internet connection!\n" +
                                           "• For absolute kid safety, turn on Airplane Mode! Your nephew can write, debug, and run games completely isolated with local data storage.\n" +
                                           "• Safe, secure, and distractions-free!",
                                    color = Color(0xFF49454F),
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilesTabScreen(
    projects: List<ProjectEntity>,
    currentProj: ProjectEntity?,
    onProjectSelect: (ProjectEntity) -> Unit,
    onProjectDelete: (ProjectEntity) -> Unit,
    onCreateNewClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Spell Grimoire 📚",
                            color = Color(0xFF1D1B20),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sandbox scripts saved offline on your phone.",
                            color = Color(0xFF49454F),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (projects.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🪄", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No saved spells yet!",
                            color = Color(0xFF1D1B20),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create your very first script below!",
                            color = Color(0xFF49454F),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(projects) { project ->
                val isSelected = currentProj?.id == project.id
                var confirmDelete by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEADDFF) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("project_card_${project.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onProjectSelect(project) }
                        ) {
                            Text(
                                text = project.name,
                                color = if (isSelected) Color(0xFF21005D) else Color(0xFF1D1B20),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saved: ${dateFormat.format(Date(project.lastModified))}",
                                color = if (isSelected) Color(0xFF49454F) else Color(0xFF49454F),
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onProjectSelect(project) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF21005D) else Color(0xFFF3EDF7),
                                    contentColor = if (isSelected) Color.White else Color(0xFF6750A4)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("load_project_button_${project.id}")
                            ) {
                                Text(
                                    if (isSelected) "ACTIVE" else "OPEN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (!confirmDelete) {
                                IconButton(
                                    onClick = { confirmDelete = true },
                                    modifier = Modifier.testTag("delete_project_init_${project.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF5350)
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = { onProjectDelete(project) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350)),
                                    modifier = Modifier.testTag("delete_project_confirm_${project.id}")
                                ) {
                                    Text("CONFIRM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                // Auto revert double-tap check after 3 seconds
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(3000)
                                    confirmDelete = false
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Create button (primary color high contrast purple fab matching High Density theme)
        FloatingActionButton(
            onClick = onCreateNewClick,
            containerColor = Color(0xFF6750A4),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_project_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Spell")
        }
    }
}

package com.example.ui.lessons

data class Lesson(
    val id: Int,
    val title: String,
    val icon: String, // Emoji
    val shortDesc: String,
    val longDesc: String,
    val sampleCode: String,
    val challenge: String
)

data class ProjectIdea(
    val id: Int,
    val title: String,
    val icon: String,
    val desc: String,
    val goal: String,
    val libs: List<String>,
    val starterCode: String
)

object LessonsData {
    val lessonsList = listOf(
        Lesson(
            id = 1,
            title = "Magic Box Secrets 📦",
            icon = "📦",
            shortDesc = "Store words and numbers inside memory boxes called variables!",
            longDesc = "Welcome! In Python, variables are like named boxes where you can store secrets like names, numbers, or words. You put values inside them using '=' and can print them or speak them out loud anytime. Let's make a greeting and play a chime!",
            sampleCode = """# Create a variable to hold your name
my_name = "Neph"
print("Hey there, " + my_name + "! Welcome!")

# Let's ask Python to get your input!
print("What is your secret spy name?")
spy_name = input()

print("Scanning credentials...")
phone.play_sound("beep")
print("Access granted, Agent " + spy_name + "! 🤫")
phone.speak("Access granted, Agent " + spy_name)
""",
            challenge = "Change the my_name box to your real name and run the code to register your secret spy credentials! 📦"
        ),
        Lesson(
            id = 2,
            title = "Decisions & Branches 🔮",
            icon = "🔮",
            shortDesc = "Teach your phone how to make decisions based on passwords!",
            longDesc = "Computers make decisions using 'if' statements. It's like checking a rule: IF you have the password, you are allowed in! Otherwise, you are locked out. Let's build a secure magic gate!",
            sampleCode = """# Try changing this password to 'abracadabra' to open the gate!
password_guess = "magic"

print("The gatekeeper stares at you...")
phone.play_sound("beep")

if password_guess == "abracadabra":
    print("✨ SUCCESS! The magical golden gates open wide!")
    phone.speak("Success! The magical gates are open.")
    phone.play_sound("success")
    phone.vibrate(200)
else:
    print("❌ CLANG! The gates remain shut. Try another word!")
    phone.speak("Access denied! Clang.")
    phone.play_sound("error")
""",
            challenge = "Change the password_guess variable at the top to 'abracadabra' and run the script! Watch the gates open. 🔐"
        ),
        Lesson(
            id = 3,
            title = "Loops & Painting 🐢",
            icon = "🐢",
            shortDesc = "Control a digital turtle to draw neon shapes using for loops!",
            longDesc = "Repeating code manually is boring! In Python, we use 'for' loops to tell the computer to repeat actions automatically. Let's control a digital turtle paint-brush to draw a neon cyan square 4 times!",
            sampleCode = """# Set up a thick neon paintbrush!
turtle.color("cyan")
turtle.width(6)

print("Constructing square sides...")

# A loop that runs 4 times (one for each side)
for side in range(4):
    print("Drawing side number", side + 1)
    turtle.forward(120)
    turtle.right(90)
    phone.play_sound("beep")

print("Square finished successfully! 🐢")
phone.play_sound("chime")
""",
            challenge = "Try changing turtle.color('cyan') to 'pink' or 'orange'! You can also increase forward(120) to 180 for a massive square. 🎨"
        ),
        Lesson(
            id = 4,
            title = "Custom Commands def 🪄",
            icon = "🪄",
            shortDesc = "Create your own reusable magic commands using def functions!",
            longDesc = "Functions are like custom magic commands. Instead of writing 10 lines of code every time, you package them under 'def' and run them with a single line! This helps organize your codes beautifully.",
            sampleCode = """# We define a custom function using 'def'
def cast_magic_spell(spell_name, sound_type):
    print("🪄 CASTING SPELL: " + spell_name + "!")
    phone.speak("Casting spell " + spell_name)
    phone.play_sound(sound_type)
    phone.vibrate(150)

# Now we call our custom spell multiple times!
cast_magic_spell("Fireball", "laser")
cast_magic_spell("Healing Charm", "success")
""",
            challenge = "Add another spell call at the bottom of the script like: cast_magic_spell('Lightning Bolt', 'alarm') and run! ⚡"
        ),
        Lesson(
            id = 5,
            title = "Intro to Pygame 🎮",
            icon = "🎮",
            shortDesc = "Learn how real game loops check events and draw graphics!",
            longDesc = "Pygame is the ultimate library for creating games! Real video games run on an infinite 'Event Loop' that runs 60 times a second to check for button presses, update sprites, and redraw screens. Let's look at a starter template!",
            sampleCode = """# Pygame is supported in Pydroid 3!
# Let's import the standard game modules
import pygame
import sys

print("Initializing Pygame screen graphics...")

# On Pydroid 3, we would run:
# pygame.init()
# screen = pygame.display.set_mode((400, 600))
# pygame.display.set_caption("My First Game")

print("Entering the Game Event Loop!")
print("1. Keeps the game window open.")
print("2. Checks if the player clicks QUIT.")
print("3. Updates the game screen continuously.")

# Simulated Pygame exit call
print("Game closed successfully!")
phone.play_sound("chime")
""",
            challenge = "Run this script to see the game initialization telemetry. You are ready to start writing real arcade games! 🎮"
        )
    )

    val projectsList = listOf(
        ProjectIdea(
            id = 1,
            title = "Magic Voice Calculator 🧮",
            icon = "🧮",
            desc = "An interactive, kid-friendly voice calculator that speaks math answers out loud!",
            goal = "Write a Python program that takes two numbers and an operator (+, -, *, /) and both prints and speaks the calculated result!",
            libs = listOf("standard input", "phone.speak()", "phone.play_sound()"),
            starterCode = """print("--- MAGIC VOICE CALCULATOR ---")
phone.speak("Welcome to Magic Calculator!")

# Get numbers from the user
print("Enter first number:")
num1 = float(input())

print("Enter second number:")
num2 = float(input())

print("Choose an operator (+, -, *, /):")
operator = input()

# Calculate the result
if operator == "+":
    result = num1 + num2
elif operator == "-":
    result = num1 - num2
elif operator == "*":
    result = num1 * num2
elif operator == "/":
    if num2 == 0:
        result = "Cannot divide by zero!"
    else:
        result = num1 / num2
else:
    result = "Invalid operator!"

print("Calculating...")
phone.play_sound("beep")

# Speak and print results!
output = "Your answer is " + str(result)
print(output)
phone.speak(output)
phone.play_sound("success")
"""
        ),
        ProjectIdea(
            id = 2,
            title = "Pygame Guess the Number 🎮",
            icon = "🎮",
            desc = "A visual Pygame guessing game that chooses random numbers and reacts!",
            goal = "Build an arcade game that generates a secret random number between 1 and 10, requests user guess, and visually flashes simulation results.",
            libs = listOf("pygame", "random", "phone.vibrate()"),
            starterCode = """import pygame
import random
import sys

print("--- PYGAME GUESS THE NUMBER ---")

# Let's pick a secret random number!
secret_number = random.randint(1, 10)
print("The computer chose a secret number between 1 and 10.")
phone.speak("Guess a number from 1 to 10")

print("Enter your guess:")
guess = int(input())

# Pygame Setup (Runs for real on Pydroid 3, simulated in PyKid!)
pygame.init()

if guess == secret_number:
    print("🎉 CORRECT! You are a master guesser!")
    phone.speak("Correct! You are a master guesser.")
    phone.play_sound("success")
    phone.vibrate(200)
    # Pygame visual screen indicator
    print("[Pygame] Flashing screen GREEN! 🟢")
else:
    print("😢 Oops! The secret number was " + str(secret_number))
    phone.speak("Wrong guess! The answer was " + str(secret_number))
    phone.play_sound("error")
    # Pygame visual screen indicator
    print("[Pygame] Flashing screen RED! 🔴")

print("Thank you for playing!")
"""
        ),
        ProjectIdea(
            id = 3,
            title = "Spiral Turtle Art 🎨",
            icon = "🎨",
            desc = "Draw spectacular spinning neon geometric art spirals!",
            goal = "Combine range loops, angles, and vibrant colors to draw a beautiful hypnotic spiral star on canvas.",
            libs = listOf("turtle.color()", "turtle.forward()", "random.choice()"),
            starterCode = """import random

print("--- SPINNING SPIRAL ART ---")
phone.speak("Watch the turtle paint some beautiful magic art!")

# Set up canvas thick neon paint brush
turtle.width(4)
colors = ["cyan", "pink", "yellow", "orange", "purple", "green"]

# We will draw 20 lines to form a cool spiral star!
for i in range(20):
    # Choose a random color for each line
    col = random.choice(colors)
    turtle.color(col)
    
    # Forward distance grows longer each step!
    dist = i * 10
    turtle.forward(dist)
    
    # Turn right by a magic angle for spirals!
    turtle.right(144)
    
    # Play a sound on each drawing line
    phone.play_sound("beep")

print("Art drawing complete! 🤩")
phone.play_sound("success")
"""
        )
    )
}

rootProject.name = "JV8"
include("src:main:ui")
findProject(":src:main:ui")?.name = "ui"

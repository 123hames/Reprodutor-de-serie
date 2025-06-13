Set objShell = CreateObject("WScript.Shell")

' Mudar para a pasta do projeto
objShell.CurrentDirectory = "C:\Users\ru3en\eclipse-workspace\PassarSerie"

' Executar o programa Java em background
objShell.Run "java -cp bin Main", 0, False

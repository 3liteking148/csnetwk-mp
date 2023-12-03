@echo off
set MODULE_PATH="javafx-21.0.1\windows\lib"
set MODULES=javafx.controls,javafx.fxml

javac --module-path %MODULE_PATH% --add-modules %MODULES% *.java

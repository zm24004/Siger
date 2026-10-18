@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2 (Windows)
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if exist "%MAVEN_PROJECTBASEDIR%.mvn\jvm.config" (
    for /f "usebackq tokens=*" %%a in ("%MAVEN_PROJECTBASEDIR%.mvn\jvm.config") do set JVM_CONFIG=%%a
    set "MAVEN_OPTS=%MAVEN_OPTS% %JVM_CONFIG%"
)
set MAVEN_CMD_LINE_ARGS=%MAVEN_CONFIG%
if exist "%MAVEN_PROJECTBASEDIR%.mvn\maven.config" (
    for /f "usebackq tokens=*" %%a in ("%MAVEN_PROJECTBASEDIR%.mvn\maven.config") do set MAVEN_CMD_LINE_ARGS=%%a
)

@REM Find java executable
if not "%JAVA_HOME%"=="" (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
        echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation. >&2
        exit /b 1
    )
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVACMD=java"
    where java >nul 2>&1
    if errorlevel 1 (
        echo ERROR: JAVA_HOME is not set and 'java' could not be found in your PATH. >&2
        echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation. >&2
        exit /b 1
    )
)

@REM The canonical path delegates to the wrapper jar if present
set "MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%MAVEN_WRAPPER_PROPERTIES%" (
    echo ERROR: Could not find %MAVEN_WRAPPER_PROPERTIES% >&2
    echo Create the file or generate the wrapper with: mvn -N wrapper:wrapper >&2
    exit /b 1
)

if exist "%MAVEN_WRAPPER_JAR%" (
    "%JAVACMD%" %MAVEN_OPTS% -classpath "%MAVEN_WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_LINE_ARGS% %*
    goto :eof
)

@REM Distribution-only mode: download Maven directly.
for /f "usebackq tokens=1,* delims==" %%a in ("%MAVEN_WRAPPER_PROPERTIES%") do (
    if "%%a"=="distributionUrl" set "DISTRIBUTION_URL=%%b"
)
if "%DISTRIBUTION_URL%"=="" (
    echo ERROR: distributionUrl not set in %MAVEN_WRAPPER_PROPERTIES% >&2
    exit /b 1
)

set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "DIST_DIR=%MAVEN_USER_HOME%\wrapper\dists"

powershell -Command "& { ^
    $url = '%DISTRIBUTION_URL%'; ^
    $dist = '%DIST_DIR%'; ^
    $name = [System.IO.Path]::GetFileNameWithoutExtension([System.IO.Path]::GetFileName($url)) -replace '-bin$',''; ^
    $dest = Join-Path $dist $name; ^
    if (Test-Path (Join-Path $dest 'bin\mvn.cmd')) { exit 0 }; ^
    New-Item -ItemType Directory -Force -Path $dest ^| Out-Null; ^
    $zip = Join-Path $dest 'maven-dist.zip'; ^
    Write-Host ('Downloading Apache Maven from ' + $url); ^
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
    Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing; ^
    Write-Host 'Extracting...'; ^
    Expand-Archive -Path $zip -DestinationPath $dest -Force; ^
    Remove-Item $zip; ^
}"
if errorlevel 1 (
    echo ERROR: Failed to download or extract Maven. >&2
    exit /b 1
)

@REM Locate the extracted mvn.cmd and run it
for /d %%D in ("%DIST_DIR%\*") do (
    if exist "%%D\bin\mvn.cmd" set "MAVEN_EXEC=%%D\bin\mvn.cmd"
)
if "%MAVEN_EXEC%"=="" (
    echo ERROR: Could not locate Maven executable after extraction in %DIST_DIR% >&2
    exit /b 1
)

"%JAVACMD%" %MAVEN_OPTS% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath NUL "%MAVEN_EXEC%" %MAVEN_CMD_LINE_ARGS% %*

endlocal

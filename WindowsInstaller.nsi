;FreeDots Install
;author: Simon Kainz <simon@familiekainz.at>
;Based on the NSIS Modern User Interface Start Menu Folder Selection Example Script


;--------------------------------
;Include Modern UI

	!include "MUI2.nsh"
	!include "Library.nsh"

;Include Font installation code
	!include "FileFunc.nsh"
	!include FontRegAdv.nsh
	!include FontName.nsh

!ifndef VERSION
  !define VERSION "0.6p1" 
!endif

!ifndef JARFILE
  !error "JARFILE is not defined"
!endif
!define READMEFILENAME "README.JAWS.txt"


;----------------------------
; Java Settings
!define JRE_VERSION "6.0"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=24936&/jre-6u10-windows-i586-p.exe"
!define JAVAEXE "javaw.exe"

;--------------------------------
;Product Info

	!define PRODUCT "FreeDots"
	!ifndef VERSION
		!error "VERSION is not defined"
	!endif

 !define MUI_WELCOMEPAGE_TITLE $(msg_WelcomePageTitle)

;--------------------------------
;General

	;Name and file
	Name "${PRODUCT}"
	Caption "${PRODUCT} ${VERSION} Setup"
	
	!ifdef OUTFILE
		OutFile "${OUTFILE}"
	!else
		OutFile "${PRODUCT}Installer.exe"
	!endif

	!ifndef DISTDIR
		!define DISTDIR "dist"
	!endif

	SetCompressor /SOLID LZMA
	SetOverwrite IfNewer

	;Default installation folder
	InstallDir "$PROGRAMFILES\${PRODUCT}"

	;Get installation folder from registry if available
	InstallDirRegKey HKLM "Software\${PRODUCT}" ""

	;Request application privileges for Windows Vista
	RequestExecutionLevel admin

;--------------------------------
;Variables

	Var StartMenuFolder

;--------------------------------
;Interface Settings

	!define MUI_ABORTWARNING

;--------------------------------
;Pages

	!insertmacro MUI_PAGE_WELCOME
	!insertmacro MUI_PAGE_LICENSE "LICENSE.txt"
	!insertmacro MUI_PAGE_DIRECTORY

;--------------------------------

	;Start Menu Folder Page Configuration
	!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM"
	!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\${PRODUCT}"
	!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"

	!insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder

	!insertmacro MUI_PAGE_INSTFILES
	!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\README.JAWS.txt"
	!insertmacro MUI_PAGE_FINISH
	!insertmacro MUI_UNPAGE_CONFIRM
	!insertmacro MUI_UNPAGE_INSTFILES





!insertmacro GetFileVersion
!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare

;--------------------------------
;Languages

!define UNINSTALLOG_LOCALIZE ; necessary for localization of messages from the uninstallation log file

;Remember the installer language
!define MUI_LANGDLL_REGISTRY_ROOT "HKCU"
!define MUI_LANGDLL_REGISTRY_KEY "Software\${PRODUCT}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "Installer Language"

!insertmacro MUI_LANGUAGE "English" ; default language
!insertmacro MUI_LANGUAGE "German"

;----------------------------
; Language strings
LangString msg_WelcomePageTitle ${LANG_ENGLISH} "Setup for ${PRODUCT}, Version ${VERSION}"
LangString msg_WelcomePageTitle ${LANG_GERMAN} "${PRODUCT} ${VERSION} Installation"

;--------------------------------
;Installer Sections

Section "install"

 	Call GetJRE
  	Pop $R0

	StrCpy $FONT_DIR $FONTS
	File fonts/UBraille.ttf
	!insertmacro InstallTTF 'fonts/UBraille.ttf'
	SendMessage ${HWND_BROADCAST} ${WM_FONTCHANGE} 0 0 /TIMEOUT=5000

	SetOutPath "$INSTDIR"
	SetShellVarContext all
	SetOverwrite IfNewer

	File dist/${JARFILE} 
	File /r ${READMEFILENAME}
	File doc\manual.html
	File doc\example*.mid

	;Store installation folder
	WriteRegStr HKLM "Software\${PRODUCT}" "" $INSTDIR

	;Create uninstaller
	WriteUninstaller "$INSTDIR\Uninstall.exe"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "UninstallString" '"$INSTDIR\uninstall.exe"'
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "DisplayName" "${PRODUCT}"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "DisplayVersion" "${VERSION}"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "NoModify" "1"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "NoRepair" "1"

	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application

		;Create shortcuts
		CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\FreeDots.lnk" "$INSTDIR\${JARFILE}"
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\README.JAWS.lnk" "$INSTDIR\${READMEFILENAME}"
		CreateShortCut "$SMPROGRAMS\$StartMenuFolder\User Manual.lnk" "$INSTDIR\manual.html"

	!insertmacro MUI_STARTMENU_WRITE_END



SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

	SetShellVarContext all

	

	RMDir /r "$INSTDIR"

	!insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuFolder

	RMDir /r "$SMPROGRAMS\$StartMenuFolder"

	DeleteRegKey HKLM "Software\${PRODUCT}"
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}"

SectionEnd



Function GetJRE
    Push $R0
    Push $R1
    Push $2
  
  ; 1) Check local JRE
    ClearErrors
    StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
    IfFileExists $R0 JreFound
  
  ; 2) Check for JAVA_HOME
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckRegistry     
    IfFileExists $R0 0 CheckRegistry
    Call CheckJREVersion
    IfErrors CheckRegistry JreFound
 
  ; 3) Check for registry
  CheckRegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors DownloadJRE
    IfFileExists $R0 0 DownloadJRE
    Call CheckJREVersion
    IfErrors DownloadJRE JreFound
  
  DownloadJRE:
    Call ElevateToAdmin
    MessageBox MB_ICONINFORMATION "${PRODUCT} uses Java Runtime Environment ${JRE_VERSION}, it will now be downloaded and installed."
    StrCpy $2 "$TEMP\Java Runtime Environment.exe"
    nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
    Pop $R0 ;Get the return value
    StrCmp $R0 "success" +3
      MessageBox MB_ICONSTOP "Download failed: $R0"
      Abort
    ExecWait $2
    Delete $2
    
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 GoodLuck
    Call CheckJREVersion
    IfErrors GoodLuck JreFound
    
  ; 4) wishing you good luck
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    ; MessageBox MB_ICONSTOP "Cannot find appropriate Java Runtime Environment."
    ; Abort
  
  JreFound:
    Pop $2
    Pop $R1
    Exch $R0
FunctionEnd

; Pass the "javaw.exe" path by $R0
Function CheckJREVersion
    Push $R1
    
    ; Get the file version of javaw.exe
    ${GetFileVersion} $R0 $R1
    ${VersionCompare} ${JRE_VERSION} $R1 $R1
    
    ; Check whether $R1 != "1"
    ClearErrors
    StrCmp $R1 "1" 0 CheckDone
    SetErrors
    
  CheckDone:
    Pop $R1
FunctionEnd


Function ElevateToAdmin
  UAC_Elevate:
    UAC::RunElevated
    StrCmp 1223 $0 UAC_ElevationAborted ; UAC dialog aborted by user?
    StrCmp 0 $0 0 UAC_Err ; Error?
    StrCmp 1 $1 0 UAC_Success ;Are we the real deal or just the wrapper?
    Quit
    
  UAC_ElevationAborted:
    # elevation was aborted, run as normal?
    MessageBox MB_ICONSTOP "This installer requires admin access, aborting!"
    Abort

  UAC_Err:
    MessageBox MB_ICONSTOP "Unable to elevate, error $0"
    Abort

  UAC_Success:
    StrCmp 1 $3 +4 ;Admin?
    StrCmp 3 $1 0 UAC_ElevationAborted ;Try again?
    MessageBox MB_ICONSTOP "This installer requires admin access, try again"
    goto UAC_Elevate 
FunctionEnd




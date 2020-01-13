### Easy Diary User Guide

#### 🔖 Table of Contents
01. [Add TTF font](#user-content-️-add-ttf-font)  
02. [Diary attached photo recovery](#user-content-️-diary-attached-photo-recovery)

#### ☑️ Add TTF font
When the device language setting is set to Korean or English, the available fonts in this diary are the three fonts provided in the diary and the fonts set in the device.  
To use the fonts you own in the diary, simply copy the TTF fonts to the specified location as follows.  

__01. Check the built-in list__  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_01_en.png" width="288" height="512">
  
__02. Check the font directory__  
If the TTF font file is in the device, use the Explorer installed on the device.   
The explorer used in the screenshot is the 'Simple File Manager' Explorer.  
If the TTF font file is on your PC, you can use the explorer available on your PC.  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_02_en.png" width="288" height="512">

__03. Copy TTF font file to font directory__  
The font directory for Easy Diary is '/AAFactory/EasyDiary/Fonts/'.  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_03_en.png" width="288" height="512">

__04. Check additional fonts__  
When a TTF file is added to the font directory, the font is added to the font settings list.  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_04_en.png" width="288" height="512">

#### ☑️ Diary attached photo recovery
Easy Diary only supports backup and recovery of content except attachments in backup and recovery operations.  
If the user wants to back up and recover the attached photo, the operation must be done manually.  
Backup and recovery of all diary content including attachments will be available in the future.     

__01. Checking the photo management directory of the backup target device__  
Easy Diary compresses the attached photo and keeps them in a separate directory.
You can check the attached photo directory by connecting the backup target device to the computer and moving to the following path.
Easy Diary photo management directory: /AAFactory/EasyDiary/Photos/

__02. Copying the photo management directory of the backup target device__  
Copy the 'Photos' directory to any location on your computer.
[Backup Device] -> [Desktop]

__03. Copy the photo management directory to the recovery target device__  
Overwrite the 'Photos' directory copied in any path to the photo management directory of the device being restored.
Easy Diary photo management directory: /AAFactory/EasyDiary/Photos/
[Desktop] -> [Restore Device]

__04. Confirm attachment recovery__  
Attachment Once the photo is restored, you can view the attached photo normally.  

📌 Before recovery  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/manual_02_01.png" width="284" height="537">

📌 After recovery  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/manual_02_02.png" width="284" height="537">

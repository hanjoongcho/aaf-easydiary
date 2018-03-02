# <span style="color:#07ABB3">📘 Easy Diary User Guide</span>

## Table of Contents
📌 [Add TTF font](#add-ttf-font)  
📌 [Diary attached photo recovery](#diary-attached-photo-recovery)

## Add TTF font
When the device language setting is set to Korean or English, the available fonts in this diary are the three fonts provided in the diary and the fonts set in the device.  
To use the fonts you own in the diary, simply copy the TTF fonts to the specified location as follows.  

#### 01. Check the built-in list
<pre>
<img src="screenshots/app_easydiary12_01_en.png" width="288" height="512">
</pre>
  
#### 02. Check the font directory
<pre>
If the TTF font file is in the device, use the Explorer installed on the device.   
The explorer used in the screenshot is the 'Simple File Manager' Explorer.  
If the TTF font file is on your PC, you can use the explorer available on your PC.
  
<img src="screenshots/app_easydiary12_02_en.png" width="288" height="512">
</pre>

#### 03. Copy TTF font file to font directory
<pre>
The font directory for Easy Diary is '/AAFactory/EasyDiary/Fonts/'.  

<img src="screenshots/app_easydiary12_03_en.png" width="288" height="512">
</pre>

#### 04. Check additional fonts
<pre>
When a TTF file is added to the font directory, the font is added to the font settings list.
  
<img src="screenshots/app_easydiary12_04_en.png" width="288" height="512">
</pre>

## Diary attached photo recovery
Easy Diary only supports backup and recovery of content except attachments in backup and recovery operations.  
If the user wants to back up and recover the attached photo, the operation must be done manually.  
Backup and recovery of all diary content including attachments will be available in the future.     

#### 01. Checking the photo management directory of the backup target device
<pre>
Easy Diary compresses the attached photo and keeps them in a separate directory.
You can check the attached photo directory by connecting the backup target device to the computer and moving to the following path.
Easy Diary photo management directory: /AAFactory/EasyDiary/Photos/
</pre>

#### 02. Copying the photo management directory of the backup target device
<pre>
Copy the 'Photos' directory to any location on your computer.
[Backup Device] -> [Desktop]
</pre>

#### 03. Copy the photo management directory to the recovery target device
<pre>
Overwrite the 'Photos' directory copied in any path to the photo management directory of the device being restored.
Easy Diary photo management directory: /AAFactory/EasyDiary/Photos/
[Desktop] -> [Restore Device]
</pre>

#### 04. Confirm attachment recovery
<pre>
Attachment Once the photo is restored, you can view the attached photo normally.

* Before recovery
<img src="screenshots/manual_02_01.png" width="284" height="537">

* After recovery
<img src="screenshots/manual_02_02.png" width="284" height="537">
</pre>
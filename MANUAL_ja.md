# <span style="color:#07ABB3">📘 イージーダイアリーユーザーガイド</span>

## 目次
📌 [TTF フォントを追加](#ttf-フォントを追加)  
📌 [ダイアリー添付写真の回復](#ダイアリー添付写真の回復)

### TTF フォントを追加
デバイスの言語設定が韓国語または英語に設定されている場合でダイアリーで使用可能なフォントは、ダイアリーで提供する3つのフォントとデバイスに設定されたフォントです.    
デバイスの言語設定が日本語の場合、デフォルトでデバイス設定フォントのみが提供されます。したがって、日記で所有するフォントを使用する場合は、次のようにTTFフォントを指定された場所にコピーできます.  

#### 01. 組み込みリストを確認
<pre>
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_01_ja.png" width="288" height="512">
</pre>

#### 02. フォントディレクトリを確認
<pre>
TTFフォントファイルがデバイスにある場合は、デバイスにインストールされてエクスプローラを利用し.   
スクリーンショットで使用されるエクスプローラは、'Simple File Manager' エクスプローラです.  
TTFフォントファイルがPCにある場合は、PCで使用可能なエクスプローラを利用します.  

<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_02_ja.png" width="288" height="512">
</pre>

#### 03. フォントディレクトリにTTFフォントファイルをコピーする
<pre>
イージーダイアリーのフォントディレクトリは '/AAFactory/EasyDiary/Fonts/' です.  

<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_03_ja.png" width="288" height="512">
</pre>

#### 04. 追加フォントを確認
<pre>
フォントディレクトリにTTFファイルが追加されると、フォントの設定のリストに対応するフォントが追加されます.
  
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/app_easydiary12_04_ja.png" width="288" height="512">
</pre>

## ダイアリー添付写真の回復
イージーダイアリーは、バックアップおよびリカバリ操作時添付写真を除いて、コンテンツのバックアップと復元のみをサポートしています。  
もしユーザーが添付写真のバックアップと復元を希望する場合は、その作業は手動で行わなければならない。  
添付写真を含む完全なダイアリーコンテンツのバックアップと復元機能は、今後提供予定です。  

#### 01. バックアップ先デバイスの写真管理ディレクトリを確認
<pre>
イージーダイアリーは、添付の写真を圧縮して、別のディレクトリに保存します。
バックアップ先デバイスをコンピュータに接続して、次のパスに移動すると、添付画像のディレクトリを確認することができます。
イージーダイアリー写真管理ディレクトリ: /AAFactory/EasyDiary/Photos/
</pre>

#### 02. バックアップ先デバイスの写真管理ディレクトリのコピー
<pre>
'Photos'ディレクトリを接続したコンピュータの任意のパスにコピーします。
[Backup Device] -> [Desktop]
</pre>

#### 03. 回復対象デバイスに写真管理ディレクトリのコピー
<pre>
任意のパスにコピーされた'Photos'ディレクトリを復元対象デバイスの写真管理ディレクトリに上書きされます。
イージーダイアリー写真管理ディレクトリ: /AAFactory/EasyDiary/Photos/
[Desktop] -> [Restore Device]
</pre>

#### 04. 添付写真の修復の確認
<pre>
添付写真の修復が完了したら、添付の写真を閲覧することができます。

* 復元前
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/manual_02_01.png" width="284" height="537">

* 復元後
<img src="https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/screenshots/manual_02_02.png" width="284" height="537">
</pre>

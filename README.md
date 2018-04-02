# Sirene Zwei

* [日本語](#japanese)
* [English](#english)

<hr id="japanese" />
<h2 id="summary-ja">概要</h2>
このリポジトリは2018年5月に行われる灘校文化祭において、アマチュア無線部のブースで展示する、リコーダー自動演奏装置「Sirene Zwei」の駆動プログラムおよびコントローラのコードをまとめたものです。  

<h2>目次</h2>

1. [概要](#summary-ja)
2. 目次
3. [Sirenと比較](#contrast-ja)

<h2 id="contrast-ja">Sirenと比較</h2>

|名称|Siren|Sirene Zwei|
|------|------|------|
|ドライバの環境|Arduino|Arduino|
|コントローラの言語|Python 3.4|Kotlin 1.2.30|
|コントローラのUI|コマンドライン|GUI|
|操作の種類|自動演奏のみ|自動演奏、手動演奏|

&emsp;上記の表はSirenとSirene Zweiの簡単な比較です。また、Sirene Zweiでは設計を大幅に見直し、Sirenと比べてスマートなデザインとなっています。　　
　自動演奏のための曲の音階、音価及び休符のシーケンスにはMIDIを採用しているので、実質的にリコーダーをMIDIデバイス化することがこのプロジェクトの目的と言って過言ではありません。
両者ともMIDIをコントローラ側で解釈してノート番号を送るという基本設計は共通しています。  
　ドライバとコントローラのインターフェース(と言うほどのモノでもないですが)はリコーダーの音域に存在する27音に00～27までの番号を振り、それを送ることによって音階を、次のシーケンスまでの時間で音価を表しています。28番は休符の代わりに予約されており、28番を送信するとすべてのソレノイドがオフ状態になり、吸気バルブも閉じます。

<hr id="english" />
<h2 id="summary-en">Summary</h2>

<i>[REQUEST FROM WRITER]  
I am a beginning English learner. Please pardon some grammar and spelling errors.  </i>

This repository is a collection of controller and driver of "Sirene Zwei" that will be exhibited in Amateur Radio Club booth of Nada School Festival what held in May, 2018.

<h2>Table of Contents</h2>

1. [Summary](#summary-en)
2. Table of Contents
3. [Compared with Siren](#contrast-en)

<h2 id="contrast-en">Compared with Siren</h2>

|Name|Siren|Sirene Zwei|
|------|------|------|
|Driver Module|Arduino|Arduino|
|Environment of Controller|Python 3.4|Kotlin 1.2.30|
|Control Interface|Command-line|GUI|
|Controlling Method|Automation Only|Auto, Manual|

The above table is a plain comparison of Siren and Sirene Zwei. In addition, Sirene Zwei has redesigned its design drastically, making it a smart design compared with Siren.  
The shortest way to complete this project is to make the recorder a MIDI device, because MIDI is adopted for the musical scale, note value and resting sequence of the song for automatic performance.
Both of those have a common basic design that interprets MIDI on the controller side and sends note numbers on accurate time of sequence.
The interface between the driver and the controller (may be too overstatement :p) uses a number ranging from 00 to 28 to the 27 notes existing in the range of the recorder and 28 is reserved instead of a cessation. And by sending it, the time which is until the next sequence is the sound value. When you send number 28 all solenoids are off and the air valves is closed.

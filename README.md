<font face="微軟正黑體">

## 1. 背景

隨著道路交通量的增長，交通事故發生率也不斷地上升，由此造成的道路堵塞大幅提高車禍救援的時間，且會產生嚴重的車輛堵塞、無法行駛與連續車禍等風險，對於駕駛者來說，如何快速找到替代道路避開因車禍而塞車的路段，是當下最需要解決的問題。

因此，為了解決以上問題，我們團隊設計與製作出一款APP，可安裝於行車紀錄器內，使用者能透過此技術得知目前發生車禍的路段，希望能即時通知大量行駛中的車輛改道，進而解決因車禍所造成的救援時間延遲、交通阻塞、連續車禍風險等問題。


## 2. 系統系統功能與架構
#### 2.1 系統簡介
本系統可自動擷取前方道路影像並判斷其是否包含車禍事件，我們以 Web 形式建立車禍查詢系統，並將已確認過的車禍資訊標記於地圖上，希望能即時通知大量行駛中的車輛改道，進而解決因車禍所造成的救援時間延遲、交通阻塞、連續車禍風險等問題。

本系統在整體架構設計上，分為分散式車禍事件偵測架構和雲端運算與裁決架構兩部分，如下圖。

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex1.jpg" width="700px" align=center />

分散式車禍事件偵測架構會自動擷取前方道路影像，連同 GPS 資訊與時間，上傳到雲端運算與裁決架構進行處理；雲端運算與裁決架構首先使用 Google Cloud Vision API 進行車禍事件偵測，再將偵測結果、 GPS 資訊、時間存於資料庫中。而在資料庫中，會選取第一筆資料作為參考點，並使用 Haversine 公式篩選出距離參考點五公尺內的資料，再將這些資料進行 Voting 機制運算，確認是否有車禍發生，再運用三角定位法，即時把車禍事件標記在雲端地圖上。

#### 2.2. 分散式車禍事件偵測架構
單一式事件偵測架構要達到車禍事件的準確性，需要龐大反覆運算的計算量，導致難以在現實生活中實現，如下圖上半部所示。因此，我們採用分散式的架構，來減少單一式偵測架構的計算量，並達到在現實生活中精確的車禍事件偵測，如圖五下半部所示。
在我們的分散式車禍事件偵測架構中，我們採用Android平台開發，可用於個人手持裝置，且易於跨平臺移植至行車紀錄器。我們的系統會自動擷取前方道路影像，並將擷取到的影像、 GPS 資訊與時間上傳到雲端運算與裁決架構。


<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex2.jpg" width="500px" align=center />


本系統在行車過程中，會自動擷取前方的道路影像，並上傳到雲端運算與裁決架構。首先由 Google Cloud Vision API 分析前方路面是否為車禍事件，並取得該影像為車禍事件的機率值，如下圖所示。


<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex3.jpg" width="700px" align=center />



再利用 GPS 定位取得自身當前所在的坐標位置，將上述兩種資訊及時間上傳至雲端運算與裁決架構。目前本系統主要是透過搭載於車輛上的 Android 手機來進行測試，如下圖所示。


<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex4.jpg" width="500px" align=center />


#### 2.3. 雲端運算與裁決架構

雲端運算與裁決架構包括 Google Cloud Vision API 、 MongoDB 資料庫、Voting機制、雲端標記四大部分。我們以 Web 的形式為使用者提供車禍事件查詢服務，在 Web 上，我們選擇 Python-Django 架構作為開發框架，在此框架下，我們通過 HTML 來搭建 Web 頁面的框架，在 HTML 中嵌入 JavaScript 腳本語言，來實現地圖的顯示功能。

__Google Cloud Vision API：__ 當本系統將資料上傳到雲端運算與裁決架構後，首先，會對前方道路影像進行 Google Cloud Vision API 智能影像分析，並將分析出的數值化結果，連同機器人上傳的 GPS 資訊與時間，傳送到 MongoDB 資料庫，為後續的 Voting 作處理，如下所示。


<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex5.jpg" width="700px" align=center />


__MongoDB資料庫：__ 在資料庫的搭建中，我們使用MongoDB資料庫儲存車禍事件的機率值(Score)、 GPS 資訊與時間，並結合 Google Map 實現即時車禍事件地圖標記，其中 MongoDB 數據庫設計，如下圖所示。


<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex6.jpg" width="500px" align=center />

雲端運算與裁決架構取得本系統上傳的資料後，會先選出第一筆數據作為參考點，並使用 Haversine 公式找出距離參考點五公尺內的數據，Haversine 公式如以下公式所示：
<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex7.jpg" align=center />

其中𝑅＝地球半徑，𝑑＝距離，Δφ＝兩點緯度的差值，φ＝緯度，Δλ＝兩點經度的差值，透過 Haversine 公式篩選出距離參考點五公尺內的數據，如下圖所示。

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex8.jpg" width="500px" align=center />

Voting機制：接著為了將誤判降到最低，我們利用 Voting 機制，少數服從多數原則，可以避免 Google Cloud Vision API 的偵測能力不足與誤判的情況。我們把距離參考點五公尺內的數據篩選出來後，將數據中機率值大於 α 的全表示為 1，其餘表示為 0，如公式所示：

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex9.jpg" width="200px" align=center />

其中Pi＝此張照片之車禍事件機率值、Si＝此張照片之車禍事件結果。再將上式中得到的數據加總，除以距離參考點五公尺內的車禍事件數量，如公式所示：

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex10.jpg" width="150px" align=center />

其中 i＝距離參考點五公尺內的車禍事件數量、R＝總體車禍事件偵測機率。最後，如果 R 大於 β，則表示此車禍事件成立，如公式所示：

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex11.jpg" width="200px" align=center />

其中 D＝群體車禍事件偵測結果。在進行 Voting 運算之前，我們根據整體的數據大致分為兩類情況：__僅少量照片測出車禍且機率偏低的特殊情況__ 以及 __多數測出機率且機率高之情況__。為了避免前者被誤判為車禍，我們採用自動化設置參數，對以上兩種情況定義不同的 α、β 值，以利 Voting 機制的計算，如公式所示：
<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex12.jpg" width="400px" align=center />

雲端標記：因為偵測到車禍的位置與實際發生車禍的位置會有誤差，所以在標記車禍地點前我們利用三角定位演算法，以減少偵測到車禍的位置與實際發生車禍的位置的誤差，使標記地點與實際地點較為相近，如下圖所示。

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex13.jpg" width="400px" align=center />

Voting機制運算結果確認車禍事件發生後，最後運用三角定位法，即時把車禍事件標記在雲端地圖上，能夠讓使用者即時查詢目前發生車禍的路段，如下圖所示。

<img src="https://github.com/veryjimmy/Traffic_jam/blob/master/picture/ex14.jpg" width="400px" align=center />






</font>

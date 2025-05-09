# Yolov5_Food_check

**Yolov5_Food_check**는 spring 프레임워크로 개발한 사이트에 접속 후 학습된 yolov5 모델을 이용하여 사진을 업로드시 음식의 이름과 칼로리를 표시해 줍니다.
- yolotest -> spring boot 웹 사이트
- yolov5-train -> 음식이 학습된 yolov5, fastapi

---

- **모델 명칭**: Yolov5
- Food_Find 웹 사이트에 포함 하거나 새로운 어플리케이션으로 만들기전 테스트 입니다.

---

## 학습 결과 - 시각화

<table>
  <tr>
    <td align="center"><b>클래스별 F1-Confidence Curve</b></td>
  </tr>
    <tr>
    <td><img src="./images/F1_curve.png" width="70%"></td>
  </tr>
  <tr>
        <td align="center"><b>데이터셋 시각화 결과</b></td>
  </tr>
    <tr>
    <td><img src="./images/labels.jpg" width="70%"></td>
  </tr>
</table>

---

## 서비스 구성도

<table>
    <tr>
    <td><img src="./images/yolov5.drawio.png" width="100%"></td>
  </tr>
  <tr>
</table>

---

### 📦 데이터셋

- **출처 링크**: [Dataset](https://universe.roboflow.com/yong331/sk-shielders-module-project-3_koreanfood_detecting)
- **클래스 수**: 총 20종

```python
names: ['Bibimbap', 'Bulgogi', 'Godeungeogui',
'Jjambbong', 'Ramyun', 'Yangnyumchicken',
'duinjangzzigae', 'gamjatang', 'gimbap',
'jeyukbokum', 'jjajangmyeon', 'kalguksu',
'kimchizzigae', 'mandu', 'pajeon', 'samgyetang',
'samgyubsal', 'sundaegukbap', 'tteokbokki', 'tteokguk']

```

## 결과 화면
<table>
    <tr>
    <td><img src="./images/site.png" width="100%"></td>
  </tr>
  </table>

---

## 📌 향후 개선 방향

- 실제 구현 모델은 YoloV8을 사용
- 클래스 수 확장(현재 한식 종류에 의해서만 학습된 상태) -> 양식, 일식 등
- 더 다양한 데이터셋 추가 확보

---

## 📮 문의

- 이메일: `qhgus9346@gmail.com`

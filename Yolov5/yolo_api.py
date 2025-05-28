from fastapi import FastAPI, UploadFile, File
from fastapi.responses import JSONResponse
from collections import defaultdict
from fastapi.staticfiles import StaticFiles
import shutil, os, subprocess
import json

app = FastAPI()
app.mount("/runs", StaticFiles(directory="yolov5-master/runs"), name="runs")

def map_class_to_label(class_id):
    class_names = ['Bibimbap', 'Bulgogi', 'Godeungeogui', 'Jjambbong', 'Ramyun', 'Yangnyumchicken', 'duinjangzzigae', 'gamjatang', 'gimbap', 'jeyukbokum', 'jjajangmyeon', 'kalguksu', 'kimchizzigae', 'mandu', 'pajeon', 'samgyetang', 'samgyubsal', 'sundaegukbap', 'tteokbokki', 'tteokguk']  # YOLO 클래스
    return class_names[class_id] if class_id < len(class_names) else "알 수 없음"

with open("calorie_map.json", "r", encoding="utf-8") as f:
    calorie_data = json.load(f)

korean_labels = { #한글 매핑
    "bibimbap": "비빔밥",
    "bulgogi": "불고기",
    "godeungeogui": "고등어구이",
    "jjambbong": "짬뽕",
    "ramyun": "라면",
    "yangnyumchicken": "양념치킨",
    "duinjangzzigae": "된장찌개",
    "gamjatang": "감자탕",
    "gimbap": "김밥",
    "jeyukbokum": "제육볶음",
    "jjajangmyeon": "짜장면",
    "kalguksu": "칼국수",
    "kimchizzigae": "김치찌개",
    "mandu": "만두",
    "pajeon": "파전",
    "samgyetang": "삼계탕",
    "samgyubsal": "삼겹살",
    "sundaegukbap": "순대국밥",
    "tteokbokki": "떡볶이",
    "tteokguk": "떡국"
}

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    filename = file.filename
    image_path = f"uploads/{filename}"
    os.makedirs("uploads", exist_ok=True)

    with open(image_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    result = run_yolo(image_path)

    return JSONResponse(content=result)

def run_yolo(image_path):
    yolov5_dir = os.path.abspath("yolov5-master")  # 실제 YOLO 폴더명으로 정확히
    abs_image_path = os.path.abspath(image_path)   # 이미지 절대경로로 변환

    subprocess.run([
        "python", "detect.py",
        "--weights", "runs/train/exp/weights/best.pt",
        "--source", abs_image_path,
        "--conf", "0.25",
        "--save-txt",
        "--exist-ok",
        "--name", "api"
    ], cwd=yolov5_dir)

    label_file = os.path.join(yolov5_dir, "runs", "detect", "api", "labels", os.path.basename(image_path).rsplit(".", 1)[0] + ".txt")
    if not os.path.exists(label_file):
        return {"status": "fail", "message": "인식 실패"}

    labels = []
    with open(label_file, "r") as f:
        lines = f.readlines()
        for line in lines:
            class_id = int(line.split()[0])
            label = map_class_to_label(class_id).strip()
            labels.append(label.lower())  # 소문자로 정규화

    # 중복 제거
    unique_labels = sorted(set(labels))

    results = []
    for label in unique_labels:
        calories = calorie_data.get(label)
        korean_label = korean_labels.get(label, label)  # 한글 매핑, 없으면 그대로
        results.append({
            "label": korean_label,
            "calories": calories
        })
    image_filename = os.path.basename(image_path)
    output_image_path = f"runs/detect/api/{image_filename}"

    return {
        "status": "ok",
        "results": results, #라벨, 칼로리
        "image": output_image_path  # 이미지
    }
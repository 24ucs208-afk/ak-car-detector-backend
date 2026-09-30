from ultralytics import YOLO
import sys
import json

model_path = r"C:\Users\acer\Downloads\ak-car-detector-backend\ak-car-detector-backend\runs\detect\train-2\weights\best.pt"

image_path = sys.argv[1]

model = YOLO(model_path)

results = model.predict(
    source=image_path,
    conf=0.25,
    verbose=False
)

detections = []

for result in results:
    for box in result.boxes:
        class_id = int(box.cls[0])
        confidence = float(box.conf[0])

        detections.append({
            "damage": result.names[class_id],
            "confidence": round(confidence, 2)
        })

print(json.dumps(detections))
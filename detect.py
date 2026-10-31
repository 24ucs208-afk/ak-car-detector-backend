from ultralytics import YOLO
import sys
import json
import os

# YOLO model path
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

model_path = os.path.join(
    BASE_DIR,
    "models",
    "best.pt"
)

# Input image path
if len(sys.argv) < 2:
    print(json.dumps({"error": "Image path is required"}))
    sys.exit(1)

image_path = sys.argv[1]

try:
    # Load trained YOLO model
    model = YOLO(model_path)

    # Detect vehicle damage
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

    # Return JSON result to Java
    print(json.dumps(detections))

except Exception as e:
    print(json.dumps({
        "error": str(e)
    }))
    sys.exit(1)
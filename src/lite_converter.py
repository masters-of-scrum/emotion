from tensorflow.contrib import lite

keras_file="model_emocions.h5"

converter = lite.TocoConverter.from_keras_model_file(keras_file)
tflite_model=converter.convert()

open("modelLite.tflite", "wb").write(tflite_model)
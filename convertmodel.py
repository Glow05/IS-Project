import sys
print(sys.path)

import tensorflow as tf
from keras.models import load_model


model = load_model("Household_tools_classification_Tensorflow_Model_for_Android_App.ipynb")

converter = tf.lite.TFLiteConvrter.from_keras_model(model)
tflite_model = converter.convert()

print("model Converted")

#Save the model.
with open('model.tflite', 'wb') as f:
    f.write(tflite_model)
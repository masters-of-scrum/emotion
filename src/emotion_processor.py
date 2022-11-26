from tensorflow.keras.layers import Lambda,Input,Dense,Flatten
from tensorflow.keras.models import Model
from tensorflow.keras.applications.efficientnet import EfficientNetB2
from tensorflow.keras.applications.efficientnet import preprocess_input
from tensorflow.keras.preprocessing import image
from tensorflow.keras.preprocessing.image import ImageDataGenerator,load_img
from tensorflow.keras.models import Sequential
import numpy as np
from glob import glob

from tensorflow import keras


IMAGE_SIZE=[224,224]
train_path='data/images/train'

valid_path='data/images/validation'
eff=EfficientNetB2(input_shape=IMAGE_SIZE+[3],include_top=False)

for layer in eff.layers:
  layer.trainable=False

folders=glob('data/images/train/*')
x=Flatten()(eff.output)

prediction=Dense(len(folders),activation='softmax')(x)

model=Model(inputs=eff.input,outputs=prediction)

model.summary()

model.compile(optimizer='adam',loss='categorical_crossentropy',metrics=['accuracy'])

train_datagen=ImageDataGenerator(rescale=1./255,shear_range=0.2,zoom_range=0.2,horizontal_flip=True)

test_datagen=ImageDataGenerator(rescale=1./255)

training_set=train_datagen.flow_from_directory(train_path,
                                               target_size=(224,224),batch_size=32,class_mode='categorical')

test_set=test_datagen.flow_from_directory(valid_path,
                                               target_size=(224,224),batch_size=32,class_mode='categorical')

model.fit(training_set,validation_data=test_set,
                      epochs=3,
                      steps_per_epoch=len(training_set),
                      validation_steps=len(test_set))

keras_file="model_emocions.h5"
try:
    keras.models.save_model(model,keras_file)
except:
    keras.models.save_model(r,keras_file)





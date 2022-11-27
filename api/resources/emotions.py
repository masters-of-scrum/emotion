"""
Handles the Emotions api
Provides:
    - Enums for all possible emotions
    - GET
    - POST
"""
import PIL.Image
from PIL import Image
from enum import Enum
import numpy as np
from flask import Response, request, jsonify
from flask_restful import Resource
import cv2
from prisma.models import Emotion
from tensorflow.keras.models import model_from_json
from io import StringIO

class ModelCheck():
    def __init__(self):
        self.modelo_json_file='./resources/model_data/model_data.json'
        self.modelo_weights_file='./resources/model_data/model_weights.h5'
        self.text_list=["Enfadat", "Disgustat", "Por", "Content", "Neutral", "Trist", "Sorpres"]

    def get_result_model(self,roi):
        with open(self.modelo_json_file, "r") as json_file:
            self.loaded_model_json=json_file.read()
            loaded_model= model_from_json(self.loaded_model_json )
            loaded_model.load_weights(self.modelo_weights_file)
            img =  np.array([float(x) for x in roi.split(',')]).reshape(48, 48)
            pred=loaded_model.predict(img[np.newaxis, :,:, np.newaxis])
            text_idx=np.argmax(pred)
            out = self.text_list[text_idx]
            return out
            


class EmotionsApi(Resource):
    modelo=ModelCheck()
    """
    Class that handles the REST methods related to the emotions detected
    """
    def get(self):
        """
        Returns all emotions stored in the DB
        """
        emotions = Emotion.prisma().find_many()
        return Response(emotions, mimetype="application/json", status=200)

    def post(self):
        """
        Given a valid string in the body request, creates a new emotion record in the db
        """
        body = request.get_json()
        result = self.modelo.get_result_model(body['image'])

        return Response(result, mimetype="application/json", status=201)





"""
Handles the Emotions api
Provides:
    - Enums for all possible emotions
    - GET
    - POST
"""
import PIL.Image
from enum import Enum
import numpy as np
from flask import Response, request
from flask_restful import Resource

from prisma.models import Emotion
from tensorflow.keras.models import model_from_json
from model_data import model_weights, model_data

class AllowedEmotion(Enum):
    """
    Provides a common model to identify allowed emotions
    """
    ANGER = 1
    NEUTRAL = 2
    HAPPY = 3
    SAD = 4
    SURPRISED = 5
    FEAR = 6
    DISGUST = 7

class ModelCheck():
    def __init__(self):
        self.modelo_json_file='./resources/model_data/model_data.json'
        self.modelo_weights_file='./resources/model_data/model_weights.h5'
        self.text_list=["Enfadat", "Disgustat", "Por", "Content", "Neutral", "Trist", "Sorpres"]

    def get_result_model(self,roi):
        with open(self.model_json_file, "r") as json_file:
            self.loaded_model_json=json_file.read()
            loaded_model= model_from_json(self.loaded_model_json)
            loaded_model.load_weights(self.modelo_weights_file)

            img = np.array([float(x) for x in roi.split(',')]).reshape(48,48);

            pred=loaded_model.predict(roi[np.newaxis, :,:, np.newaxis])
            text_idx=np.argmax(pred)
            return self.text_list[text_idx]



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

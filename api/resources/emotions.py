"""
Handles the Emotions api
Provides:
    - Enums for all possible emotions
    - GET
    - POST
"""
from enum import Enum

from flask import Response, request
from flask_restful import Resource

from prisma.models import Emotion

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


class EmotionsApi(Resource):
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
        enum_type = AllowedEmotion[body.type.lower()]
        if isinstance(enum_type, AllowedEmotion):
            emotion = Emotion.prisma().create(data={'type': enum_type.name})
            return Response(emotion, mimetype="application/json", status=201)

        return Response('Bad request', status=400)

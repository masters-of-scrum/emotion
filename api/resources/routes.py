"""
Utility file that allows for API route initialization
"""
from .emotions import EmotionsApi


def initialize_routes(api):
    """
    Initializes all the API routes
    """
    api.add_resource(EmotionsApi, '/api/emotions')

from .emotions import EmotionsApi

def initialize_routes(api):
    api.add_resource(EmotionsApi, '/api/emotions')
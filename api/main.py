"""
Main script to initialize the api
"""
from flask import Flask
from flask_restful import Api

from prisma import Prisma, register

from resources.routes import initialize_routes


db = Prisma()
db.connect()
register(db)
app = Flask(__name__)
api = Api(app)

if __name__ == "__main__":
    initialize_routes(api)
    app.run(port=80)

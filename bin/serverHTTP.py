from http.server import BaseHTTPRequestHandler, HTTPServer
import logging
from datetime import datetime

# The following class handles the creation of a server that can work with
# POST and GET methods / the HTTP protocol


class S(BaseHTTPRequestHandler):
    def _set_response(self):
        self.send_response(200)  # Set response code
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def interpret_POST(self, post_data):
        # The POST message from the phone is parsed to retrieve key informations, such as the time,
        # the coordinates and the ID of the phone
        s = post_data.decode('utf-8')
        L = s.split("\n")
        date, hour = str(datetime.now()).split(" ")
        ID = ":".join(str(str(self.headers).split("\n")[5]).split(":")[1:])[1:]
        longitude = float(L[4])
        latitude = float(L[9])
        print(
            f"Date : {date}\nHeure : {hour}\nID : {ID}\nCoordonnées GPS : {(latitude, longitude)}\n")
        # The result is saved in a .csv file
        f.write(f"{ID},{date + ':' + hour},{latitude},{longitude}\n")

    def do_GET(self):
        # This function sets up the response string if a GET query is sent from the phone
        #logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
        self._set_response()
        self.wfile.write("Connection established".encode('utf-8'))

    def do_POST(self):
        # This function sets up the response string if a POST message is
        # received from the phone
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        # logging.info("POST request,\nPath: %s\nHeaders:\n%s\n\nBody:\n%s\n",
        # str(self.path), str(self.headers), post_data.decode('utf-8'))
        self.interpret_POST(post_data)
        self._set_response()
        self.wfile.write("Bien reçu !".encode('utf-8'))

# The following functions starts the server, which will listen to localhost, port 5000
# A ngrok server will connect to localhost port 5000, and open this local Python server to
# the world through a public URL


def run(server_class=HTTPServer, handler_class=S, port=5000):
    logging.basicConfig(level=logging.INFO)
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    logging.info('Starting httpd...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    f.close()
    httpd.server_close()
    logging.info('Stopping httpd...\n')


if __name__ == '__main__':
    # Each time coordinates are received by the sever, these are saved into a
    # .csv file.
    f = open("coords.csv", "w")
    f.write("ID,Heure,Latitude,Longitude\n")
    run()

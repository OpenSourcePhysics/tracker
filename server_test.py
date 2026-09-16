import http.server
import urllib.parse
import sys

class Handler(http.server.SimpleHTTPRequestHandler):
    def do_POST(self):
        if self.path.startswith('/log'):
            length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(length).decode('utf-8', errors='replace')
            print(f"[BROWSER LOG]: {body}", flush=True)
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b"OK")
        else:
            super().do_POST()

    def do_GET(self):
        if self.path.startswith('/log'):
            query = urllib.parse.urlparse(self.path).query
            print(f"[BROWSER GET LOG]: {query}", flush=True)
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b"OK")
        else:
            super().do_GET()

if __name__ == '__main__':
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8890
    server = http.server.HTTPServer(('127.0.0.1', port), Handler)
    print(f"Serving on port {port}", flush=True)
    server.serve_forever()

import urllib.request as req
import json

projectId = "5c595ece619046147bb9a152"
baseUrl = "http://localhost:8888/"
requ = req.Request(f'{baseUrl}projects/{projectId}/sessions/new', method='POST')
resp = req.urlopen(requ)
payload = resp.read().decode()
session = json.loads(payload)
sessionId = session['id']
print("created session: ", session)

headers = {'Content-Type': 'application/json'}
data = {'path': 'a/simple.mo', 'content': "model simple\nend simple;"}

requ = req.Request(f"{baseUrl}sessions/{sessionId}/files/update", data=json.dumps(data).encode(), headers=headers, method='POST')
resp = req.urlopen(requ)
print("created file: ", resp.getcode(), resp.read().decode())


data['content'] = "model err e err;"
requ = req.Request(f"{baseUrl}sessions/{sessionId}/files/update", data=json.dumps(data).encode(), headers=headers, method='POST')
resp = req.urlopen(requ)
print("updated file: ", resp.getcode(), resp.read().decode())


compileData = {'path': 'a/simple.mo'}
requ = req.Request(f"{baseUrl}sessions/{sessionId}/compile", data=json.dumps(compileData).encode(), headers=headers, method='POST')
resp = req.urlopen(requ)
print("compiled session: ", resp.getcode(), resp.read().decode())

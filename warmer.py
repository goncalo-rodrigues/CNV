import requests
import time

URL ='http://127.0.0.1:8000'
FILES = {"test01.txt","test02.txt","test03.txt","test04.txt","test05.txt"}

session = requests.session()

def makeRequest(file,sc,sr,wc,wr,coff,roff):
    url = URL+"/r.html?f="+file+"&sc="+str(sc)+"&sr="+str(sr)+"&wc="+str(wc)+"&wr="+str(wr)+"&coff="+str(coff)+"&roff="+str(roff)
    print("\n"+url);
    result = session.get(url)
    print (result.text)

start_time = time.time()
for file in FILES:
    '''
    makeRequest(file, sc=400, sr=400, wc=100, wr=400, coff=0, roff=0)
    makeRequest(file, sc=400, sr=400, wc=100, wr=400, coff=300, roff=0)
    makeRequest(file, sc=400, sr=400, wc=200, wr=100, coff=100, roff=0)
    makeRequest(file, sc=400, sr=400, wc=200, wr=100, coff=100, roff=300)
    makeRequest(file, sc=400, sr=400, wc=200, wr=200, coff=100, roff=100)'''
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=0, roff=0)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=0, roff=100)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=0, roff=200)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=0, roff=300)

    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=100, roff=0)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=100, roff=100)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=100, roff=200)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=100, roff=300)

    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=200, roff=0)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=200, roff=100)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=200, roff=200)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=200, roff=300)

    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=300, roff=0)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=300, roff=100)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=300, roff=200)
    makeRequest(file, sc=400, sr=400, wc=100, wr=100, coff=300, roff=300)


    print(time.time() - start_time)

#http://127.0.0.1:8000/r.html?f=test04.txt&sc=1000&sr=1000&wc=600&wr=160&coff=0&roff=650

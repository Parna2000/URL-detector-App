from os.path import join,dirname
import pickle

# url_prediction = pickle.load(open('url_checker.pkl', 'rb'))

def main(s):
    filename = join(dirname(__file__),"url_checker.pkl")
    bloom= pickle.load(open(filename, 'rb'))
    y=bloom.__contains__(s)
    if (y==True):
        return "Malicious URL received"
    else:
        return "Non-malicious URL received"
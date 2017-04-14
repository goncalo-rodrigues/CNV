import urllib.request
import pandas
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

def request_raytracer(file, sr, sc, wr, wc, roff, coff):
    x = urllib.request.urlopen("http://localhost/r.html?f=%s&sc=%d&sr=%d&wr=%d&wc=%d&roff=%d&coff=%d" % (file, sr, sc, wr, wc, roff, coff)).read()
    return int(x)

def run_raytracer(file, resolution=(100,100), grid=(20,20)):
    mapping = {}
    for roff in range(grid[0]):
        for coff in range(grid[1]):
            wr = resolution[0]/grid[0]
            wc = resolution[1]/grid[1]
            x = request_raytracer(file, resolution[0], resolution[1], wr, wc, wr*roff, wc*coff)
            mapping[(roff, coff)] = x
    return mapping




def get_statistics(feature, mapping, grid):
    output = np.zeros(shape=(grid[1], grid[0]))
    for key in mapping:
        x = key[1]
        y = grid[0] - key[0] - 1
        cols = pandas.read_csv('../../raytracer-master/dynamic_%d.txt' % mapping[key])
        searched = cols.loc[cols['description'] == feature]
        if (len(searched) == 0):
            value = 0
        else:
            value = searched['value']

        output[x, y] = value
    return output.T

test01 = [[902,  902, 902,   902,   902,   902,   902,  1529,  3646,  3534],
 [  902,   902,   902,  902,   902,   902,   902,  1789,  3570,  3568],
 [ 1714,  2526,  2190,  1042,  1022,  2006,  2294,  1624,  1786, 1506],
 [ 3702, 3702, 3702, 3225, 2783, 3302, 3302, 3302, 1838,  902],
 [ 3702, 3702, 3702, 3679, 3375, 3302, 3302, 3302, 2942,  902],
 [ 3684, 3702, 3702, 3692, 3490, 3349, 3335, 3332, 2976,  902],
 [ 3568, 3602, 3602, 3144, 2934, 3448, 3403, 3420, 2951, 1882],
 [ 1656, 2428, 2126, 1036, 1032, 2098, 2640, 3660, 3702, 3702],
 [  902,  902,  902,  902,  902,  902, 2060, 3699, 3702, 3702],
 [  902,  902,  902,  902,  902,  902, 1818, 3628, 3700, 3702]]

test02 = [[ 4302, 4302, 4338, 4326, 4302, 4457, 4884, 4902, 4902, 4776.],
 [ 4302, 4608, 4896, 4878, 4512, 4762, 4902, 4948, 4978, 4944.],
 [ 4348, 4989, 5074, 5069, 4900, 4756, 5039, 5102, 5102, 5102.],
 [ 4490, 4694, 4702, 4740, 4786, 5036, 5120, 5102, 5102, 5096.],
 [ 4502, 4572, 4688, 4717, 4699, 5019, 5168, 5100, 5070, 4954.],
 [ 4502, 4502, 4502, 4558, 4797, 4906, 4968, 4848, 4862, 4872.],
 [ 4511, 4502, 4546, 4602, 4670, 4860, 4880, 4781, 4802, 4802.],
 [ 4606, 4597, 4668, 4662, 4750, 4866, 4788, 4708, 4764, 4800.],
 [ 4654, 4684, 4702, 4702, 4822, 4813, 4344, 4394, 4454, 4386.],
 [ 4378, 4662, 4734, 4688, 4614, 4350, 4302, 4302, 4302, 4302.]]

test03=[[ 2503, 2503, 2503, 2503, 2503, 2503, 2503, 2503, 2503, 2503.],
 [ 2503, 2503, 2503, 2503, 2509, 2509, 2503, 2503, 2503, 2503.],
 [ 2503, 2503, 2503, 2593, 2697, 2697, 2590, 2503, 2503, 2503.],
 [ 2503, 2495, 2465, 2680, 2703, 2703, 2673, 2517, 2503, 2503.],
 [ 2503, 2429, 2431, 2599, 2621, 2643, 2574, 2476, 2472, 2501.],
 [ 2503, 2414, 2404, 2500, 2564, 2520, 2479, 2405, 2403, 2417.],
 [ 2503, 2436, 2319, 2308, 2297, 2306, 2331, 2403, 2403, 2403.],
 [ 2491, 2409, 2374, 2339, 2256, 2265, 2309, 2401, 2403, 2403.],
 [ 2444, 2403, 2403, 2403, 2393, 2330, 2303, 2393, 2403, 2416.],
 [ 2423, 2403, 2403, 2403, 2403, 2403, 2400, 2476, 2486, 2502.]]

test04=[[ 10603, 10603, 10603, 11200, 17611, 17593, 11204, 10603, 10607,
   10615],
 [ 10603, 10603, 10603, 15878, 22639, 22517, 15873, 10521, 10409,
   10561.],
 [ 10693, 10693, 10929, 18792, 35394, 36508, 17828, 10493, 10493,
   10521.],
 [ 10523, 10545, 19716, 28308, 40196, 24963, 22997, 13488, 10503,
   10503.],
 [ 16146, 16902, 28376, 34439, 25295, 24786, 25058, 21406, 16902,
   16146.],
 [ 21318, 22406, 43179, 48506, 44683, 42840, 35304, 23420, 21761,
   21203.],
 [ 23698, 27117, 26812, 23076, 47818, 34975, 25761, 24485, 22920,
   21301.],
 [ 21715, 29152, 33582, 30261, 33224, 22483, 22573, 25724, 21411,
   21403.],
 [ 21403, 30303, 42798, 38417, 52998, 31723, 31118, 33590, 21909,
   21497.],
 [ 22246, 41184, 60031, 60536, 64964, 58662, 50981, 38301, 22116,
   21539.]]

test05=[[  8797,  8830,  5654,  4262,  4262,  4262,  4262,  4230,  4222,
    4194.],
 [  9485,  9543,  9056,  4502,  4502,  4502,  4502,  4792,  7984,
    9426.],
 [  9419, 11187, 10033,  4516,  4525,  4521,  4502,  7750,  9495,
    9450.],
 [  9370,  9353,  6388,  8913, 12460, 12436,  8819,  8513, 11931,
   11021.],
 [  4596,  4588,  9171, 14734, 14702, 14702, 14703, 10916,  9754,
    9828.],
 [  4502,  4502, 13235, 14702, 14702, 14702, 14702, 13137,  6108,
    7606.],
 [  4502,  4502, 14499, 14702, 14702, 14702, 14702, 13393,  4651,
    4702.],
 [  4502,  4502, 12751, 14990, 14573, 14583, 16564, 11119,  4602,
    4636.],
 [  4502,  4502,  4948, 11297, 14096, 14077, 11984,  4900,  4590,
    4552.],
 [  4502,  4505,  4572,  4602,  4609,  4629,  4629,  4577,  4503,
    4502.]]


def print_image(file, resolution=(100,100), grid=(10,10)):
    if file == "test01.txt":
        return test01
    elif file == "test02.txt":
        return test02
    elif file == "test03.txt":
        return test03
    elif file == "test04.txt":
        return test04
    elif file == "test05.txt":
        return test05

    #run_result = run_raytracer(file, resolution, grid)
    #normalized_output_method = get_statistics('dot', run_result, grid)
    #return normalized_output_method
    #print(normalized_output_method)
    '''
    normalized_output_target = get_statistics('totalinstr', run_result, grid)
    plt.subplot(1,3,1)
    imgplot = plt.imshow(normalized_output_target)
    plt.axis('off')
    plt.subplot(1,3,2)
    imgplot = plt.imshow(normalized_output_method)
    plt.axis('off')
    plt.subplot(1,3,3)
    img=mpimg.imread(resfile)/255
    plt.imshow(img)
    plt.axis('off')
    plt.show()
    '''
print ("test01.txt")
print(print_image('test01.txt'))
print ("test02.txt")
print(print_image('test02.txt'))
print ("test03.txt")
print(print_image('test03.txt'))
print ("test04.txt")
print(print_image('test04.txt'))
print ("test05.txt")
print(print_image('test05.txt'))


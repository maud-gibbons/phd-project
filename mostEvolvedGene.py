import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import cm

from collections import Counter

choice = input("-->")
raw = pd.read_csv(choice+".csv")
data = np.array(raw)
genes = [""]*len(data)

for j in range(len(data)):
   for i in range(len(data[0])):
        genes[j] += str(data[j][i])

counts = Counter(genes).most_common(8)

colours = cm.Set1(np.arange(8)/8.)
keys, values = zip(*counts)
explode = (0.1, 0, 0, 0, 0, 0, 0, 0 )

plt.axis("equal")
plt.pie(values,labels=keys,colors=colours, explode=explode, shadow=True, startangle=350,autopct='%d%%')
plt.show()

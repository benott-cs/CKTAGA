import matplotlib.pyplot as plt
import csv
import pandas as pd
import numpy as np
import sys

from sklearn import linear_model, datasets
from matplotlib.ticker import FormatStrFormatter

dataset = pd.read_csv('CKTA_Vs_Accuracy.txt', header=None, names=['CKTA', 'Accuracy'])

x = dataset['CKTA'].values
y = dataset['Accuracy'].values

#plt.plot(x, y, '.')

#print(' '.join(map(str, x)))  

print("Before linear model")
fitX=x.reshape(x.shape[0],-1)
lr = linear_model.LinearRegression()
lr.fit(fitX, y)
# Robustly fit linear model with RANSAC algorithm
print("Before RANSAC model")
ransac = linear_model.RANSACRegressor()
ransac.fit(fitX, y)
inlier_mask = ransac.inlier_mask_
outlier_mask = np.logical_not(inlier_mask)
# Predict data of estimated models
line_X = fitX
line_y = lr.predict(line_X)
line_y_ransac = ransac.predict(line_X)
#print ("X axis values for regressed lines")
#print(line_X)

# Compare estimated coefficients
print("Estimated coefficients (linear regression, RANSAC):")
print(lr.coef_, ransac.estimator_.coef_)
lw=1
plt.xlim(min(x) - 0.1, max(x) + 0.1)
plt.ylim(min(y) - 0.1, max(y) + 0.1)
#plt.scatter(x, y, color='grey', marker = '.', label='All')
plt.scatter(x[inlier_mask], y[inlier_mask], color='yellowgreen', marker='+',
            label='Inliers')
plt.scatter(x[outlier_mask], y[outlier_mask], color='gold', marker='x',
            label='Outliers')
plt.xticks(rotation=90)
plt.gca().yaxis.set_major_formatter(FormatStrFormatter('%.2f')) # 2 decimal places
plt.gca().xaxis.set_major_formatter(FormatStrFormatter('%.2f')) # 2 decimal places
plt.title('CKTA vs Accuracy')
plt.plot(line_X, line_y, color='navy', linewidth=lw, label='Linear regressor')
plt.plot(line_X, line_y_ransac, color='cornflowerblue', linewidth=lw, label='RANSAC regressor')
plt.xlabel('CKTA')
plt.ylabel('Accuracy')


# show plot
plt.legend(loc='upper left')
plt.show()

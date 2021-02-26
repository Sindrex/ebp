import pandas as pd
import numpy as np
import itertools 

def bike(typ, make, price, dat, stat, user=None, station=None, charging_level=100, total_km=0, total_trips=0, lng=None, lat=None):
    base = "("+", \t\t".join( [repr(x) if x is not None else "NULL"   for x in (typ,make,price,dat,stat,user,station,charging_level,total_km, total_trips,lng, lat)]) + ")"
    return base
         
types = ["urban", "urban", "youth", "tandem", "womens"]
makes = ["Hunday", "Kawazaki", "DBSY", "We They Peopl"]
typemakes = itertools.product(types,makes)
prices = [14000, 14000, 12000, 15000, 13000,
          14500, 14500, 12300, 15900, 13250,
          15500, 15500, 16000, 14000, 13000,
          17000, 18000, 17000, 19000, 16000]
typemakes = ((t[0][0], t[0][1],t[1]) for t in zip(typemakes, prices))
typemakes = (x for x in 52*list(typemakes))

dates = pd.date_range("2018-01-01","2018-12-31")
dates = ("%d-%02d-%02d" % (d.year,d.month,d.day) for d in 3*list(dates) )

docks = [[1,63.43049, 10.39506, 100],
        [2,63.361909, 10.377526, 200],    
        [5,63.439828, 10.462645, 200],     
        [6,63.427992, 10.375735, 200],       
        [8,63.440294, 10.400506, 100],     
        [9,63.419687, 10.401699, 100],     
        [10,63.435302, 10.409524, 100],
        [3,63.427057, 10.392525, 25],
        [4,63.436700, 10.398820, 50],
        [7,63.422357, 10.431935, 50]]

docked = []
for dock in docks:  
    for i in range(int(0.855*dock[3])): #gives 958. Each dock will be at about 85.5% capacity.
        docked.append(bike(*next(typemakes), next(dates), "DOCKED", station=dock[0], lng=dock[1], lat=dock[2]))

docked.append(bike(*next(typemakes), next(dates), "DOCKED", station=dock[0], lng=dock[1], lat=dock[2]))
docked.append(bike(*next(typemakes), next(dates), "DOCKED", station=dock[0], lng=dock[1], lat=dock[2]))

# read from file pos.list
plist = [l.rstrip().split(',') for l in open('pos.list').readlines()]
print(plist)
lats = [float(p[0]) for p in plist] 
lngs = [float(p[1]) for p in plist]
pos = iter(zip(lats,lngs))

#lats = iter(np.linspace(63.442, 63.352, 5))
#lngs = iter(np.linspace(10.3369, 10.4605, 10))
#pos = itertools.product(lats,lngs)
renteds = []
users = [1,2,3,4,5]
users = users*6
for user in users:
    lat, lng = next(pos)
    print(lat, lng)
    renteds.append(bike(*next(typemakes), next(dates), "RENTED", user, lng=lng, lat=lat))

repairs = []
descriptions = ["Deflated tire",     "Maintenance",                  "Battery replacement",
                "Disfigured wheel replacement", "Frame replacement"]*2
for d in descriptions:
    repairs.append(bike(*next(typemakes), next(dates), "REPAIR"))

with open("bike_test_gen.sql", 'w') as file:
    file.write("INSERT INTO bike (type, make, price, date_purchased, active_status, user_id, station_id, charging_level, total_km, total_trips, position_long, position_lat) VALUES \n")
    file.write(",\n".join(renteds+docked+repairs))
    file.write(";")
    file.write("\nINSERT INTO repair (request_date, description, bike_id) VALUES \n")
    descriptions=iter(descriptions)
    file.write(",\n".join( ("( "+repr(next(dates))+", "+repr(next(descriptions))+", "+ str(i)+")"  for i in range(991,1001)) ))
    file.write(";")

START TRANSACTION;
DELETE FROM repair_return;
DELETE FROM repair;
DELETE  FROM bike_status;
DELETE FROM bike;
DELETE FROM bike_type;

INSERT INTO bike_type (name) VALUES ("youth");
INSERT INTO bike_type (name) VALUES ("tandem");
INSERT INTO bike_type (name) VALUES ("womens");
INSERT INTO bike_type (name) VALUES ("urban");

COMMIT ;
SET AUTOCOMMIT=TRUE;

/*INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, active_status)
VALUES ({"urban", "urban", "youth", "tandem", "womens"},
        {"Hunday", "Kawazaki", "DBSY", "We They Peopl"},
        pri,
        {spread across 1 year},
        {dock_lat},
        {dock_long},
        {dock_id},
        "DOCKED"); * 960

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, active_status)
VALUES ({"urban", "urban", "youth", "tandem", "womens"},
{"Hunday", "Kawazaki", "DBSY", "We They Peopl"},
    pri,
    {spread across 1 year},
    {dock_lat},
    {dock_long},
    {dock_id},
    "RENTED"); * 50

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, active_status)
VALUES ({"urban", "urban", "youth", "tandem", "womens"},
    {"Hunday", "Kawazaki", "DBSY", "We They Peopl"},
    pri,
    {spread across 1 year},
    {dock_lat},
    {dock_long},
    {dock_id},
    "REPAIR"); * 10;

INSERT INTO bike (bike_id, type, make, price, date_purchased, active_status, user_id, station_id, status_timestamp, charging_level, total_km, total_trips, position_long, position_lat)


INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 1,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 2,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 3,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 4,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 5,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 6,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 7,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 8,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 9,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 10, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.435302, 10.409524, 11, 11, "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 21,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 12,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 13,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 14,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 15,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 16,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 17,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 18,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 19,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 20, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.435302, 10.409524, 11, 31, "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 41,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 22,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 23,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 24,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 25,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 26,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 27,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 28,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 29,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 30, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.435302, 10.409524, 11, 51, "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 61,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 52,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 53,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 54,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 55,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 56,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 57,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 58,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 59,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 60, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.435302, 10.409524, 11, 71, "DOCKED");

-- INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 61,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 62,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 63,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 64,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 65,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 66,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 67,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 68,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 69,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 70, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.435302, 10.409524, 11, 91, "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 101, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 72,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 73,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 74,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 75,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 76,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 77,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 78,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 79,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 80, "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 81,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 82,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 83,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 84,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 85,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 86,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 87,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 88,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 89,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 90, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.435302, 10.409524, 11, 92, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.43049, 10.39506,   1, 93,   "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.361909, 10.377526, 2, 94,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.427057, 10.392525, 3, 95,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.436700, 10.398820, 4, 96,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.439828, 10.462645, 5, 97,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.427992, 10.375735, 6, 98,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("tandem", "diamond", 200, current_date(), 63.422357, 10.431935, 7, 99,   "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.440294, 10.400506, 8, 100,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.419687, 10.401699, 9, 102,  "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.412134, 10.402053, 10, 103, "DOCKED");

INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.435302,  10.409524, 11, 104, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.43049,   10.39506,   1, 105, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.361909,  10.377526, 2, 106, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.427057,  10.392525, 3, 107, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.436700,  10.398820, 4, 108, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("offroad", "diamond", 200, current_date(), 63.439828,  10.462645, 5, 109, "DOCKED");
INSERT INTO bike (type, make, price, date_purchased, position_lat, position_long, station_id, bike_id, active_status) VALUES ("urban", "diamond", 200, current_date(), 63.427992,  10.375735, 6, 110, "DOCKED");

/*                       type, make, price,
INSERT INTO bike_status (type, make, price, position_lat, position_long, station_id, bike_id, active_status) VALUES (63.435302, "urban", "diamond", 200, current_date(), 10.409524, 11, 104, "DOCKED");
INSERT INTO bike_status (position_lat, position_long, station_id, bike_id, active_status) VALUES (63.43049, "urban", "diamond", 200, current_date(), 0.39506,   1, 105, "DOCKED");
INSERT INTO bike_status (position_lat, position_long, station_id, bike_id, active_status) VALUES (63.361909, "urban", "diamond", 200, current_date(), 10.377526, 2, 106, "DOCKED");
INSERT INTO bike_status (position_lat, position_long, station_id, bike_id, active_status) VALUES (63.427057, "urban", "diamond", 200, current_date(), 10.392525, 3, 107, "DOCKED");
INSERT INTO bike_status (position_lat, position_long, station_id, bike_id, active_status) VALUES (63.436700, "urban", "diamond", 200, current_date(), 10.398820, 4, 108, "DOCKED");
INSERT INTO bike_status (position_lat, position_long, station_id, bike_id, active_status) VALUES (63.439828, "urban", "diamond", 200, current_date(), 10.462645, 5, 109, "DOCKED");
INSERT INTO bike_status (position_lat, position_long, station_id, bike_id, active_status) VALUES (63.427992, "urban", "diamond", 200, current_date(), 10.375735, 6, 110, "DOCKED");

INSERT INTO repair_return (repair_id, return_date, price, description) VALUES (1, "2018-04-05", 5000, "Something got fixed.");
INSERT INTO repair_return (repair_id, return_date, price, description) VALUES (2, current_date(), 2400000, "Something got fixed.");
INSERT INTO repair_return (repair_id, return_date, price, description) VALUES (3, current_date(), 200, "Something got fixed.");

COMMIT;
SET AUTOCOMMIT = TRUE;
*/
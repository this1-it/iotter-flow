drop table dedalo.measures;


CREATE TABLE dedalo.measures
(key varchar, ts timestamp, tr timestamp, val float, pid varchar, sn varchar, mu varchar, lbl varchar, qual int, err varchar, own varchar, ok boolean, bm blob, PRIMARY KEY (key, ts, tr)) WITH CLUSTERING ORDER BY (ts ASC);



create index measures_own on dedalo.measures (own);
create index measures_sn on dedalo.measures (sn);
create index measures_ok on dedalo.measures (ok);
create index measures_err on dedalo.measures (err);


drop table dedalo.measures_ts_tmp;
drop table dedalo.measures_idx_tmp;


truncate dedalo.measures_ts_tmp;
truncate dedalo.measures_idx_tmp;

UPDATE COLUMN FAMILY dedalo.measures_ts_tmp with GC_GRACE = 0;
UPDATE table dedalo.measures_ts_tmp with GC_GRACE = 0;


create table dedalo.measures_ts_tmp (uid varchar, ts timestamp, primary key ((uid), ts)) with gc_grace = 10;
create table dedalo.measures_idx_tmp (uid varchar, id int, ts timestamp, primary key ((uid), id)) ;
nodetool flush dedalo measures_ts_tmp

drop table dedalo.roll_up_min15;
drop table dedalo.roll_up_h1;
drop table dedalo.roll_up_d1;
drop table dedalo.roll_up_w1;
drop table dedalo.roll_up_m1;

create table dedalo.roll_up_min15 (key varchar, ts timestamp, errts timestamp, mints timestamp, maxts timestamp, val float, minval float, maxval float, err varchar, rec bigint, count int, primary key ((key), ts)) ;

create table dedalo.roll_up_h1 (key varchar, ts timestamp, errts timestamp, mints timestamp, maxts timestamp, val float, minval float, maxval float, err varchar, rec bigint, count int, primary key ((key), ts));

create table dedalo.roll_up_d1 (key varchar, ts timestamp, errts timestamp, mints timestamp, maxts timestamp, val float, minval float, maxval float, err varchar, rec bigint, count int, primary key ((key), ts)) ;

create table dedalo.roll_up_w1 (key varchar, ts timestamp, errts timestamp, mints timestamp, maxts timestamp, val float, minval float, maxval float, err varchar, rec bigint, count int, primary key ((key), ts)) ;

create table dedalo.roll_up_m1 (key varchar, ts timestamp, errts timestamp, mints timestamp, maxts timestamp, val float, minval float, maxval float, err varchar, rec bigint, count int, primary key ((key), ts)) ;

drop table dedalo.roll_up_stats;
create table dedalo.roll_up_stats (key varchar, lbl varchar, own varchar, freq float, first timestamp, last timestamp, created timestamp, updated timestamp, running boolean, rec bigint, PRIMARY KEY ((key))) ;
create index roll_up_stats_own on dedalo.roll_up_stats (own);

truncate dedalo.roll_up_min15;
truncate dedalo.roll_up_h1;
truncate dedalo.roll_up_d1;
truncate dedalo.roll_up_w1;
truncate dedalo.roll_up_m1;
truncate dedalo.roll_up_stats;

ALTER TABLE dedalo.roll_up_stats ADD rec bigint;

ALTER TABLE dedalo.roll_up_stats ADD lbl varchar;


create index roll_up_min15_val on dedalo.roll_up_min15 (val);
create index roll_up_h1_val on dedalo.roll_up_h1 (val);
create index roll_up_d1_val on dedalo.roll_up_d1 (val);
create index roll_up_m1_val on dedalo.roll_up_m1 (val);

create index roll_up_h1_count on dedalo.roll_up_m1 (count);



CREATE KEYSPACE demodb WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : '1'};

CREATE TABLE demodb.users (
  id uuid,
  name text,
 PRIMARY KEY (id)
);
CREATE TABLE users
(
  user_id VARCHAR PRIMARY KEY NOT NULL,
  token VARCHAR UNIQUE NOT NULL,
  last_updated TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE notification_failures
(
  user_id VARCHAR NOT NULL REFERENCES users(user_id),
  notification_data VARCHAR NOT NULL,
  time_created TIMESTAMP WITH TIME ZONE NOT NULL
);
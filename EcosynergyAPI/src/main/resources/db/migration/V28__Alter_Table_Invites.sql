ALTER TABLE invites
DROP CONSTRAINT fk_sender;

ALTER TABLE invites
DROP CONSTRAINT fk_recipient;

ALTER TABLE invites
DROP CONSTRAINT fk_team;

ALTER TABLE invites
ADD CONSTRAINT fk_sender
FOREIGN KEY (sender_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE invites
ADD CONSTRAINT fk_recipient
FOREIGN KEY (recipient_id)
REFERENCES users(id)
ON DELETE CASCADE;

ALTER TABLE invites
ADD CONSTRAINT fk_team
FOREIGN KEY (team_id)
REFERENCES teams(id)
ON DELETE CASCADE;
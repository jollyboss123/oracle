CREATE SEQUENCE IF NOT EXISTS stocks_seq START WITH 1 INCREMENT BY 50;
create table stocks (
                        id bigint not null,
                        created_on timestamp without time zone,
                        updated_on timestamp without time zone,
                        ticker text unique check ( length(ticker) < 6 ) not null,
                        name text check ( length(name) < 255 ),

                        CONSTRAINT pk_stocks PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS job_details_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE job_details
(
    id              BIGINT NOT NULL,
    name            text unique check ( length(name) < 255 ) not null,
    cron_expression text check ( length(cron_expression) < 255 ) not null,
    is_active       BOOLEAN,
    created_on      TIMESTAMP WITHOUT TIME ZONE,
    updated_on      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_job_details PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS job_triggers_seq START WITH 1 INCREMENT BY 50;
CREATE TABLE job_triggers
(
    id             BIGINT NOT NULL,
    job_details_id BIGINT not null,
    name           text check ( length(name) < 255 ) not null,
    status         text check ( length(status) < 50 ) not null,
    created_on     TIMESTAMP WITHOUT TIME ZONE,
    updated_on     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_job_triggers PRIMARY KEY (id)
);

ALTER TABLE job_triggers
    ADD CONSTRAINT FK_JOB_TRIGGERS_ON_JOB_DETAILS FOREIGN KEY (job_details_id) REFERENCES job_details (id);

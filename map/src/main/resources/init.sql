CREATE SEQUENCE IF NOT EXISTS stocks_seq START WITH 1 INCREMENT BY 50;
create table stocks (
                        id bigint not null,
                        created_on timestamp without time zone,
                        updated_on timestamp without time zone,
                        ticker text unique check ( length(ticker) < 6 ) not null,
                        name text check ( length(name) < 255 ),

                        CONSTRAINT pk_stocks PRIMARY KEY (id)
);

SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_database = utf8mb4;
SET character_set_results = utf8mb4;
SET character_set_server = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;
SET collation_database = utf8mb4_unicode_ci;
SET collation_server = utf8mb4_unicode_ci;


CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_ticket` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
SOURCE /opt/sql/schema-ticket.sql;
SOURCE /opt/sql/data-ticket.sql;


CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_order_0` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_order_1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
SOURCE /opt/sql/schema-order.sql;


CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_pay_0` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_pay_1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
SOURCE /opt/sql/schema-pay.sql;


CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_user_0` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/ `12306_user_1` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
SOURCE /opt/sql/schema-user.sql;
SOURCE /opt/sql/data-user.sql;

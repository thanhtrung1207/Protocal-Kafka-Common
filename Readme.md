<h3>Docker-compose Redis</h3>

```bash
version: '3.3'
services:
  redis:
    image: redis:latest
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - /home/trungtt6/docker/redis/config/redis.conf:/usr/local/etc/redis/redis.conf#CONFIG
    environment:
      - REDIS_PASSWORD=redis
      - REDIS_PORT=6379
      - REDIS_DATABASES=16
    command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
```
    
    - tạo một folder redis.conf
    - thay thế /home/trungtt6/docker/redis/config/redis.conf = đường dẫn thực tế


    
<h3>Docker-compose Elastic</h3>
  - Tạo folder kibana cùng cấp với docker-compose 
    + Thêm file kibana.yaml vào folder kibana
kibana.yaml

```bash
xpack.security.enabled: true
xpack.ingestManager.fleet.tlsCheckDisabled: true
xpack.encryptedSavedObjects.encryptionKey: "something_at_least_32_characters"
elasticsearch.hosts: ["http://elasticsearch2:9200"]
elasticsearch.username: elastic
elasticsearch.password: elastic
server.host: 0.0.0.0
```

```bash
version: "3"
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.9.2
    container_name: elasticsearch
    environment:
      - xpack.monitoring.enabled=true
      - xpack.security.enabled=true
      - xpack.security.authc.api_key.enabled=true
      - discovery.type=single-node
      - xpack.watcher.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - ELASTIC_PASSWORD=elastic
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    restart: unless-stopped

  kibana:
    image: docker.elastic.co/kibana/kibana:7.9.2
    container_name: kibana
    volumes:
      - ./kibana:/opt/kibana/config
    environment:
      - KIBANA_PASSWORD=kibana
    depends_on:
      - elasticsearch
    ports:
      - "5601:5601"
    restart: unless-stopped

volumes:
  elasticsearch-data:
```
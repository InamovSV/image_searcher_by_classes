from aiokafka import AIOKafkaConsumer
import asyncio
from aiokafka import AIOKafkaProducer
from resnet18_cuda import NN
import json
import traceback
import sys
from os import getenv
#import ssl

def serializer(value):
    return json.dumps(value).encode()

def deserializer(serialized):
    return json.loads(serialized)


#ssl = ssl._create_unverified_context(cafile="kafka_folder/caroot.crt",
#                                     certfile="kafka_folder/localhost.crt",
#                                     keyfile="kafka_folder/localhost.key")

nn = NN()
loop = asyncio.get_event_loop()
bservers = getenv("bootstrap_servers", "kafka:9093")
inputTopic = getenv("input_topic", "neural_task")
outputTopic = getenv("output_topic", "backward_task")

print("bservers: " + bservers)
print("inputTopic: " + inputTopic)
print("outputTopic: " + outputTopic)

consumer = AIOKafkaConsumer(
    inputTopic,
    loop=loop, bootstrap_servers=bservers,
    value_deserializer=deserializer,
    key_deserializer=lambda key: key.decode("ASCII") if key else "",
    #security_protocol="SSL",
    #ssl_context=ssl,
)

producer = AIOKafkaProducer(
        loop=loop, bootstrap_servers=bservers,
        value_serializer=serializer,
        compression_type="gzip",
        #security_protocol="SSL",
        #ssl_context=ssl,
    )


async def send_mess(mess, id_ans):
    ans = list(map(int, mess))
    data = {'id': id_ans, 'indexes': ans}
    try:
        await producer.send_and_wait(outputTopic, data)
    finally:
        pass

async def consume():
    await consumer.start()
    await producer.start()
    try:
        async for msg in consumer:
            print("consumed: ", msg.topic, msg.partition, msg.offset,
                  msg.key, msg.value, msg.timestamp)
            try:
                ans = nn.predict(msg.value['path'], msg.value['type'], msg.value['indexes'])
            except Exception as e:
                ans = [11, 12, 13]
                traceback.print_exc(file=sys.stdout)
            print(ans)
            await send_mess(ans, msg.value['id'])

    finally:
        await consumer.stop()
        await producer.stop()

loop.run_until_complete(consume())

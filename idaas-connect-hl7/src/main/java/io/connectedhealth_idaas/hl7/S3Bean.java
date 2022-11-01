package io.connectedhealth_idaas.hl7;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class S3Bean  {

    private AmazonS3 client;

    @Value("${aws.access.key}")
    public String accessKey;

    @Value("${aws.secret.key}")
    public String secretKey;

    @Value("${aws.bucket}")
    public String bucket;

    @Value("${aws.region.lower}")
    public String region;


    public S3Bean(){
    }

    public void list(Exchange exchange) throws Exception {

        StringBuilder klist = new StringBuilder();
        klist.append("[");

        ObjectListing  list = (ObjectListing) exchange.getIn().getBody();

        for (int i = 0; i < list.getObjectSummaries().size(); i++){
            if(i == 0){
                klist.append(list.getObjectSummaries().get(i).getKey());
            }else{
                klist.append(",");
                klist.append(list.getObjectSummaries().get(i).getKey());
            }
        }
        klist.append("]");
        exchange.getIn().setBody(klist.toString());

    }

    public void extract(Exchange exchange, @Header("file-name") String key) throws Exception {

        S3Object object = getClient().getObject(bucket, key);

        if (object != null){
            String text = new String(object.getObjectContent().readAllBytes(), StandardCharsets.UTF_8);
            exchange.getIn().setBody(text);
        }

    }

    private AmazonS3 getClient(){
        if(client ==  null){

            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(credentials);

            client =  AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(provider).build();
        }

        return client;
    }
}

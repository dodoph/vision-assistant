package annotate;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.TextAnnotation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AnnotateHelper {
	public static enum Status {
		OK, ERROR;
	}
	
	public static Status annotate(Image img, StringBuilder result) {

		// Not required when running on GCE because the default service account to run
		// GCE VM can do authentication directly.
		GoogleCredentials myCredentials = null;
		try {
			myCredentials = GoogleCredentials
					.fromStream(new FileInputStream("/Users/jessietang/Downloads/vissist-b916a6477319.json"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return Status.ERROR;
		} catch (IOException e) {
			e.printStackTrace();
			return Status.ERROR;
		}
		
		ImageAnnotatorSettings imageAnnotatorSettings = null;
		try {
			imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
.setCredentialsProvider(FixedCredentialsProvider.create(myCredentials)).build();
		} catch (IOException e) {
			e.printStackTrace();
			return Status.ERROR;
		}

		// Initiate a client
		try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(imageAnnotatorSettings)) {
			// Build annotate request
			List<AnnotateImageRequest> requests = new ArrayList<>();
			//java class 
			Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
			//provide pic
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
			requests.add(request); //put into batch
			
			//
			AnnotateImageResponse response = vision.batchAnnotateImages(requests).getResponsesList().get(0);

			if (response.hasError()) {
				System.out.printf("Error: %s\n", response.getError().getMessage());
				return Status.ERROR;
			}

			// For full list of available annotations, see http://g.co/cloud/vision/docs
			TextAnnotation annotation = response.getFullTextAnnotation();
			result.append(annotation.getText());
		} catch (IOException e) {
			e.printStackTrace();
			return Status.ERROR;
		}
		return Status.OK;
	}
}




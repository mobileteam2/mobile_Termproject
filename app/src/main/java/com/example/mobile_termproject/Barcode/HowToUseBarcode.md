# Code
```java
public class Barcode{}
```
바코드 관련 기능 클래스.
- 사진 촬영 및 저장
- 바코드 인식 
- 바코드 정보 -> 상품 정보 반환 (예정)

```java
public void launchCamera(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        checkCameraPermission(activity);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (storageDir != null && !storageDir.exists()) {
                    storageDir.mkdirs();
                }

                photoFile = new File(storageDir, "image.png");
                photoUri = FileProvider.getUriForFile(
                        activity,
                        activity.getPackageName() + ".fileprovider",
                        photoFile
                );
                
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); 
                activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "파일 생성 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("DEBUG", "ERROR: " + e.getMessage());
            }
        }
    }
```
Intent 로 카메라 실행 후, 촬영한 사진을 앱 내 저장공간에 저장.

``` xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <!--
    - 바코드 인식 목적
        1. 바코드 사진 촬영
        2. 앱 내 Pictures 저장 경로에 barcode_images라는 이름으로 저장
    -->
    <external-files-path
        name="barcode_images"
        path="Pictures/"/>
</paths>
```
정장 경로 설정 및
```
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />

<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32"
    tools:ignore="ScopedStorage" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
      
<application
    ...
    <provider
        android:authorities="${applicationId}.fileprovider"
        android:name="androidx.core.content.FileProvider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths"/>
    </provider>
</application>
```
`AndroidManifest.xml` 파일 내 권한 및 provider 추가.

```java
public void checkCameraPermission(Activity activity){
    if(ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(activity,
                new String[]{android.Manifest.permission.CAMERA},
                REQUEST_PERMISSION_CAMERA);
    }
}
```
카메라 사용 권한이 있는지 확인. 
함수 실행 후 결과는 액티비티 파일 내에서

```java
import androidx.appcompat.app.AppCompatActivity;

public class SomeActivity extends AppCompatActivity {
    ...
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        barcode.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
```
이와 같이 `onRequestPermissionsResult` 함수가 자동적으로 실행된다. `onActivityResult`와 비슷, Intent 처리 후 자동적으로 실행.

```java
 public void onRequestPermissionsResult(Activity activity, int requestCode,
                                           String[] permission, int[] grantResults){
    if(requestCode == REQUEST_PERMISSION_CAMERA){
        if(grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED){
            launchCamera(activity);
        } else{
            Toast.makeText(activity, "카메라 권한 필요", Toast.LENGTH_SHORT).show();
        }
    }
}
```
`barcode` 클래스 내 권한 요청 여부 확인 함수. 
성공적일시, 다시 `launchCamera` 함수를 실행한다. -> 카메라 실행 -> 권환 확인 (있음) -> 촬영 후 앱 내 저장.
-> 저장된 이미지는 바코드 인식 함수로 전달할 예정.

```java
intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); 
activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
```
다시 `launchCamera`함수로 돌아와서, 해당 Intent 실행 후, 사진 촬영 및 이미지 저장 성공 시, 액티비티 내에서는 다음이 실행된다.
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == barcode.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

        String imagePath = barcode.getPhotoFile().getAbsolutePath();
        barcode.detectBarcode(imagePath, this, new Barcode.BarcodeResultListner() {
            @Override
            public void onSuccess(String barcodeValue) {
                Log.d("BARCODE", "바코드 인식 성공: " + barcodeValue);
            }

            @Override
            public void onFailure(String errMsg) {
                Log.d("BARCODE", "바코드 인식 실패: " +  errMsg);
            }
        });
    }
}
```
`imagePath`가 위에서 촬영 후 저장한 이미지의 경로.
먼저 `detectBarcode`함수의 정의는 다음과 같다.

```java
public void detectBarcode(
            String imagePath,
            Context context,
            BarcodeResultListner listner
    ){
    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
    if(bitmap == null){
        return;
    }
    InputImage image = InputImage.fromBitmap(bitmap, 0);
    BarcodeScanner scanner = BarcodeScanning.getClient();

    scanner.process(image)
            .addOnSuccessListener(barcodes -> {
                if (barcodes.isEmpty()){
                    listner.onFailure("can' find barcode");
                } else {
                    listner.onSuccess(barcodes.get(0).getRawValue());
                }
            })
            .addOnFailureListener(e ->
                    listner.onFailure("fail to read barcode: " + e.getMessage()));
}

public interface BarcodeResultListner {
    void onSuccess(String barcodeValue);
    void onFailure(String errMsg);
}
```
해당 경로의 이미지 -> bitmap -> 바코드 인식 -> barcodes.get(0)에 저장

```java
public interface BarcodeResultListner {
    void onSuccess(String barcodeValue);
    void onFailure(String errMsg);
}
```
이는 바코드 인식 과정에서 성공 및 실패 시 어떤 기능을 수행할 것인지에 대한 인터페이스
이는 액티비티에서 해당 `detectBarcode`함수를 호출할 때 같이 객체를 선언한다.
```java
barcode.detectBarcode(imagePath, this, new Barcode.BarcodeResultListner() {
        @Override
        public void onSuccess(String barcodeValue) {
            Log.d("BARCODE", "바코드 인식 성공: " + barcodeValue);
            //여기에 상품 정보 가져오는 코드 추가 예정
        }

        @Override
        public void onFailure(String errMsg) {
            Log.d("BARCODE", "바코드 인식 실패: " +  errMsg);
        }
    });
```
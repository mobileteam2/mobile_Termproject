from flask import Flask, request, jsonify
from flask import Response
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
import os
app = Flask(__name__)

cache = {}

@app.route('/getInfo', methods=['GET'])
def get_product_info():
    
    barcode = request.args.get('barcode')
    print("✅ 요청 들어옴:", barcode)
    if not barcode:
        return jsonify({'error': 'No barcode provided'}), 400

    if barcode in cache:
        return Response(cache[barcode], mimetype='text/plain')
    
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")  # 💡 중요
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")
    chrome_options.add_argument("--disable-extensions")
    chrome_options.add_argument("--disable-images")
    chrome_prefs = {
        "profile.default_content_settings": {"images": 2},
    }
    chrome_options.experimental_options["prefs"] = chrome_prefs
    
    # -------------------------------------
    # chromeDirver가 시스템 환경변수에 설정된 경우
    # -------------------------------------
    #driver = webdriver.Chrome(options=chrome_options)
    
    # -------------------------------------
    # chromeDirver가 시스템 환경변수에 설정되지 않은 경우, 직접 경로 설정
    # -------------------------------------
    #service = Service(executable_path="\chromedriver.exe")
    base_dir = os.path.dirname(os.path.abspath(__file__))
    driver_path = os.path.join(base_dir, "chromedriver.exe")
    service = Service(executable_path=driver_path)
    driver = webdriver.Chrome(service=service, options=chrome_options)
    
    try:
        #url = 'https://www.koreannet.or.kr/front/allproduct/prodSrchList.do'
        url = 'https://www.koreannet.or.kr/front/koreannet/gtinSrch.do'
        driver.get(url)

        search_input = driver.find_element(By.ID, 'gtin')
        search_input.clear()
        search_input.send_keys(barcode)

        submit_btn = driver.find_element(By.CSS_SELECTOR, ".submit")
        submit_btn.click()

        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "div.cont .nm"))
        )

        product_name_elem = driver.find_element(By.CSS_SELECTOR, "div.cont .nm")
        product_name = product_name_elem.text.strip()
        
        cache[barcode] = product_name
        
        return Response(product_name, mimetype='text/plain')
    

    except Exception as e:
        return jsonify({'error': str(e)}), 500

    finally:
        driver.quit()

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0",port=5000)
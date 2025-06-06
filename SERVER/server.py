from flask import Flask, request, jsonify
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
app = Flask(__name__)

@app.route('/getInfo', methods=['GET'])
def get_product_info():
    barcode = request.args.get('barcode')
    if not barcode:
        return jsonify({'error': 'No barcode provided'}), 400

    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--disable-gpu")
    chrome_options.add_argument("--no-sandbox")

    # -------------------------------------
    # chromeDirver가 시스템 환경변수에 설정된 경우
    # -------------------------------------
    driver = webdriver.Chrome(options=chrome_options)
    
    # -------------------------------------
    # chromeDirver가 시스템 환경변수에 설정되지 않은 경우, 직접 경로 설정
    # -------------------------------------
    #service = Service(executable_path="\chromedriver.exe")
    #driver = webdriver.Chrome(service=service, options=chrome_options)
    
    try:
        url = 'https://www.koreannet.or.kr/front/allproduct/prodSrchList.do'
        driver.get(url)

        search_input = driver.find_element(By.ID, 'searchText')
        search_input.clear()
        search_input.send_keys(barcode)

        submit_btn = driver.find_element(By.CSS_SELECTOR, "input.submit")
        submit_btn.click()

        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "div.item"))
        )

        product_name_elem = driver.find_element(By.CSS_SELECTOR, "div.item .nm")
        product_name = product_name_elem.text.strip()
        
        return product_name

    except Exception as e:
        return jsonify({'error': str(e)}), 500

    finally:
        driver.quit()

if __name__ == '__main__':
    app.run(debug=True, port=5000)
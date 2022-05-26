
import 'dart:convert';

import 'package:http/http.dart' as http;
String baseURL2 = 'https://bodhiai.live/';
Future getEasebuzz(body, key) async {
  

  var headers = {
    'Authorization': 'token $key',
    "Accept": "application/json",
    "Content-Type": "application/x-www-form-urlencoded"
  };

  var url = Uri.parse(baseURL2 + 'api/management/pay_with_easebuzz_gateway/');
  print(url);
  var data = await http.post(url, body: body, headers: headers);
  print(body);
  print(data.toString());
  if (data.statusCode == 200) {
    var utfDecode = utf8.decode(data.bodyBytes);
    var responseCode = json.decode(utfDecode);
    print(responseCode);

    return responseCode;
  } else {
    print(data.statusCode);
    print(data.body);
  }
}
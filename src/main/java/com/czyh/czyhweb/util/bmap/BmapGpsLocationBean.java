package com.czyh.czyhweb.util.bmap;

import java.io.Serializable;

public class BmapGpsLocationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private int status;
	
	Result result;
	
	public class Result{
		
		private String formatted_address;

		AddressComponent addressComponent;
		
		public String getFormatted_address() {
			return formatted_address;
		}

		public void setFormatted_address(String formatted_address) {
			this.formatted_address = formatted_address;
		}

		public class AddressComponent {
	
			private String city;
	
			private int adcode;
	
			public String getCity() {
				return city;
			}
	
			public void setCity(String city) {
				this.city = city;
			}
	
			public int getAdcode() {
				return adcode;
			}
	
			public void setAdcode(int adcode) {
				this.adcode = adcode;
			}
	
		}
		public AddressComponent getAddressComponent() {
			return addressComponent;
		}
		
		public void setAddressComponent(AddressComponent addressComponent) {
			this.addressComponent = addressComponent;
		}
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

}
/*
 *  Copyright 2014 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cmu.lti.oaqa.util;


public class EvidencingResult {
	  float mTopscore;		  // the largest score
	  float mDiscountScore;   // discounted score
	  long 	mQty;		      // the number of elements returned

	  public EvidencingResult(Float topScore, Float discScore, long qty) {
		  this.mTopscore = topScore;
		  this.mDiscountScore = discScore;
		  this.mQty = qty;
	  }
}

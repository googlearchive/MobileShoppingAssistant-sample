/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "ViewHelper.h"
#import "GTMOAuth2ViewControllerTouch.h"

@implementation ViewHelper
int const kDetailTitleCharLengthPerLine = 45;
int const kViewHelperDetailTitleHeightPerLine = 30;
int const kViewHelperDefaultCellHeight = 44;
int const kViewHelperDefaultCellImageWidth = 70;

+ (void)showToolbarSpinnerForViewController:(UIViewController *)viewController {
  UIActivityIndicatorView *spinner = [[UIActivityIndicatorView alloc]
      initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
  [spinner startAnimating];

  viewController.navigationItem.rightBarButtonItem =
      [[UIBarButtonItem alloc] initWithCustomView:spinner];
}

+ (void)showSigninToolBarButtonForViewController:
        (UIViewController *)viewController {
  viewController.navigationItem.leftBarButtonItem =
      [[UIBarButtonItem alloc] initWithTitle:@"Sign in"
                                       style:UIBarButtonItemStylePlain
                                      target:viewController
                                      action:@selector(signInPressed:)];
}

+ (void)showSignoutToolBarButtonForViewController:
        (UIViewController *)viewController {
  viewController.navigationItem.leftBarButtonItem =
      [[UIBarButtonItem alloc] initWithTitle:@"Sign out"
                                       style:UIBarButtonItemStylePlain
                                      target:viewController
                                      action:@selector(signOutPressed:)];
}

+ (void)showPopup:(NSString *)title
          message:(NSString *)message
           button:(NSString *)label {
  UIAlertView *popupView = [[UIAlertView alloc] initWithTitle:title
                                                      message:message
                                                     delegate:nil
                                            cancelButtonTitle:label
                                            otherButtonTitles:nil];
  [popupView show];
}

+ (CGFloat)heightForCellDetailWithLineCount:(NSInteger)textLength {
  return (textLength / kDetailTitleCharLengthPerLine) * kViewHelperDetailTitleHeightPerLine
          + kViewHelperDefaultCellHeight;
}

@end

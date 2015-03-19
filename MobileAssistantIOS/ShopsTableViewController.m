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

#import <CoreLocation/CoreLocation.h>
#import "GTLShoppingAssistant.h"
#import "GTLService.h"
#import "GTMHTTPFetcher.h"
#import "GTMHTTPFetcherLogging.h"
#import "GTMOAuth2Authentication.h"
#import "OffersTableViewController.h"
#import "ShopsTableViewController.h"
#import "ViewHelper.h"

@interface ShopsTableViewController ()
@property(nonatomic, strong) GTLServiceShoppingAssistant *service;
@property(nonatomic, strong) CLLocationManager *locationManager;
@property(nonatomic, strong) CLLocation *currentLocation;
@end

@implementation ShopsTableViewController {
  NSLengthFormatter *_lengthFormatter;
}
NSString *const kKeyChainName = @"MobileAssistantIOS";
NSString *const kKeyClientID = @"{{{YOUR CLIENT ID}}}";
NSString *const kKeyClientSecret = @"{{{YOUR CLIENT SECRET}}}";
NSString *const kShopToOfferSequeID = @"showOffersDetails";
NSString *const kShopTableCellID = @"ShopPrototypeCell";

@synthesize shops = _shops;
@synthesize service = _service;
@synthesize auth = _auth;
@synthesize locationManager = _locationManager;
@synthesize currentLocation = _currentLocation;

#pragma mark - Customized setters and getters

- (void)setShops:(NSArray *)list {
  if (_shops != list) {
    _shops = [list copy];

    if (self.tableView.window) {
      [self.tableView reloadData];

      if (self.navigationItem.rightBarButtonItem) {
        self.navigationItem.rightBarButtonItem = nil;
      }
    }
  }
}

- (GTLServiceShoppingAssistant *)service {
  if (!_service) {
    _service = [[GTLServiceShoppingAssistant alloc] init];

    _service.retryEnabled = YES;

    // Development only
    [GTMHTTPFetcher setLoggingEnabled:YES];
  }

  return _service;
}

#pragma mark - Authentication buttons listeners

- (IBAction)signOutPressed:(id)sender {
  [self unAuthenticateUser];
  [ViewHelper showSigninToolBarButtonForViewController:self];
}

- (IBAction)signInPressed:(id)sender {
  [self authenticateUser];
  [ViewHelper showSignoutToolBarButtonForViewController:self];
}

#pragma mark - Authentication model

// Show user login view
- (void)showUserLoginView {
  GTMOAuth2ViewControllerTouch *oauthViewController;
  oauthViewController = [[GTMOAuth2ViewControllerTouch alloc] initWithScope:@"email profile"
              clientID:kKeyClientID
          clientSecret:kKeyClientSecret
      keychainItemName:kKeyChainName
              delegate:self
      finishedSelector:@selector(viewController:finishedWithAuth:error:)];

  [self presentViewController:oauthViewController animated:YES completion:nil];
}

- (void)authenticateUser {
  if (!self.auth) {
    // Instance doesn't have an authentication object, attempt to fetch from
    // keychain.  This method call always returns an authentication object.
    // If nothing is returned from keychain, this will return an invalid
    // authentication
    self.auth = [GTMOAuth2ViewControllerTouch
        authForGoogleFromKeychainForName:kKeyChainName
                                clientID:kKeyClientID
                            clientSecret:kKeyClientSecret];
  }

  // Now instance has an authentication object, check if it's valid
  if ([self.auth canAuthorize]) {
    // Looks like token is good, reset instance authentication object
    [self resetAccessTokenForCloudEndpoint];
  } else {
    // If there is some sort of error when validating the previous
    // authentication, reset the authentication and force user to login
    self.auth = nil;
    [self showUserLoginView];
  }
}

// Reset access token value for authentication object for Cloud Endpoint.
- (void)resetAccessTokenForCloudEndpoint {
  GTMOAuth2Authentication *auth = self.auth;
  if (auth) {
    [self.service setAuthorizer:auth];

    // Add a sign out button
    [ViewHelper showSignoutToolBarButtonForViewController:self];

    // Reload the table if it's on screen
    if (self.tableView.window) {
      [self.tableView reloadData];
      if (self.navigationItem.rightBarButtonItem && self.shops) {
        self.navigationItem.rightBarButtonItem = nil;
      }
    }
  }
}

// Callback method after user finished the login.
- (void)viewController:(GTMOAuth2ViewControllerTouch *)oauthViewController
      finishedWithAuth:(GTMOAuth2Authentication *)auth
                 error:(NSError *)error {
  [self dismissViewControllerAnimated:YES completion:nil];

  if (error) {
    [ViewHelper showPopup:@"Error"
                  message:@"Failed to authenticate user"
                   button:@"OK"];
    NSLog(@"Auth error: %@", error);
  } else {
    self.auth = auth;
    [self resetAccessTokenForCloudEndpoint];
  }
}

// Signing user out and revoke token
- (void)unAuthenticateUser {
  [GTMOAuth2ViewControllerTouch removeAuthFromKeychainForName:kKeyChainName];
  [GTMOAuth2ViewControllerTouch revokeTokenForGoogleAuthentication:self.auth];
  [self.auth reset];
}

#pragma mark - View first loaded

- (void)viewDidLoad {
  [super viewDidLoad];

  // Init localized number formatter
  _lengthFormatter = [[NSLengthFormatter alloc] init];
  _lengthFormatter.numberFormatter.locale = [NSLocale currentLocale];

  // Turn on logging
  [GTMHTTPFetcher setLoggingEnabled:YES];

  // Show the spinner
  [ViewHelper showToolbarSpinnerForViewController:self];

  // Authenticate user if needed
  [self authenticateUser];

  // Get current location
  self.locationManager = [[CLLocationManager alloc] init];
  self.locationManager.delegate = self;
  self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
  if ([self.locationManager
          respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
    [self.locationManager requestWhenInUseAuthorization];
  }

  [self.locationManager startUpdatingLocation];
}

#pragma mark - Prepare Segue from "Shops" screen to "Offers/Recommendations" screen

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier
                                  sender:(UITableViewCell *)sender {
  if ([identifier isEqual:kShopToOfferSequeID]) {
    if ([self.auth canAuthorize]) {
      return YES;
    } else {
      [ViewHelper showPopup:@"Warning"
                    message:@"Please sign in first before checkin"
                     button:@"OK"];
    }
  }

  return NO;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
  if ([[segue identifier] isEqual:kShopToOfferSequeID]) {
    UINavigationController *navController = [segue destinationViewController];
    OffersTableViewController *offersViewController =
        (OffersTableViewController *)[navController topViewController];

    // Check in first, which will then pull all offers and recommendations
    NSIndexPath *myIndexPath = [self.tableView indexPathForSelectedRow];
    GTLShoppingAssistantPlaceInfo *place = [self.shops objectAtIndex:myIndexPath.row];
    [self checkIn:place nextController:offersViewController];

    // Set a spinner for the in the goodies controller
    [ViewHelper showToolbarSpinnerForViewController:offersViewController];
  }
}

#pragma mark - Location manager

- (void)locationManager:(CLLocationManager *)manager
       didFailWithError:(NSError *)error {
  NSLog(@"locationManager:didFailWithError: %@", error);

  [ViewHelper showPopup:@"Error"
                message:@"Failed to get current location."
                 button:@"OK"];
}

- (void)locationManager:(CLLocationManager *)manager
    didUpdateToLocation:(CLLocation *)newLocation
           fromLocation:(CLLocation *)oldLocation {
  // Update location once receive the GPS update from the phone
  self.currentLocation = newLocation;
  [self.locationManager stopUpdatingLocation];

  // Gets all the shops from backend only when the current location is set
  [self fetchAllShops];
}

#pragma mark - UITableView data source for shops

- (NSInteger)tableView:(UITableView *)tableView
    numberOfRowsInSection:(NSInteger)section {
  return [self.shops count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  NSString *CellIdentifier = kShopTableCellID;
  UITableViewCell *cell =
      [tableView dequeueReusableCellWithIdentifier:CellIdentifier
                                      forIndexPath:indexPath];
  UIImage *image = [UIImage imageNamed:@"shopping_cart"];
  cell.imageView.contentMode = UIViewContentModeScaleAspectFit;
  cell.imageView.clipsToBounds = YES;
  cell.imageView.image = image;

  GTLShoppingAssistantPlaceInfo *place =
      [self.shops objectAtIndex:indexPath.row];

  cell.textLabel.text = place.name;
  cell.detailTextLabel.numberOfLines = @2;

  float distance = [place.distanceInKilometers floatValue];
  NSString *distanceString = [_lengthFormatter stringFromMeters:distance*1000];
  cell.detailTextLabel.text = [NSString stringWithFormat:@"Distance: %@\n%@", distanceString, place.address];

  return cell;
}

// Helps with readability of information
- (CGFloat)tableView:(UITableView *)tableview
    heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return kViewHelperDefaultCellHeight + kViewHelperDetailTitleHeightPerLine;
}

#pragma mark - Shops Model

- (void)fetchAllShops {
  CLLocationCoordinate2D location = self.currentLocation.coordinate;
  if (CLLocationCoordinate2DIsValid(location)) {
    NSString *latitude = [NSString stringWithFormat:@"%.8f", location.latitude];
    NSString *longitude =
        [NSString stringWithFormat:@"%.8f", location.longitude];

    GTLQueryShoppingAssistant *query =
      [GTLQueryShoppingAssistant queryForPlacesGetPlacesWithLongitude:longitude
                                    latitude:latitude
                                distanceInKm:100
                                       count:100];

    [self.service
             executeQuery:query
        completionHandler:^(GTLServiceTicket *ticket,
                            GTLShoppingAssistantPlaceInfoCollection *object,
                            NSError *error) {
          if (error) {
            [ViewHelper showPopup:@"Error"
                          message:@"Unable to query a list of shops"
                           button:@"OK"];
            NSLog(@"fetchAllSops error:%@", error);
          } else {
            self.shops = [object items];
          }
        }];
  } else {
    NSLog(@"Current location is invalid: %.8f, %.8f", location.latitude,
          location.longitude);
  }
}

#pragma mark - Checkin Model

- (void)checkIn:(GTLShoppingAssistantPlaceInfo *)place
    nextController:(OffersTableViewController *)nextController {
  GTLShoppingAssistantCheckIn *checkIn =
      [[GTLShoppingAssistantCheckIn alloc] init];
  checkIn.placeId = [place.placeId stringValue];
  GTLQueryShoppingAssistant *checkinQuery = [GTLQueryShoppingAssistant
      queryForCheckinsInsertCheckInWithObject:checkIn];

  [self.service
           executeQuery:checkinQuery
      completionHandler:^(GTLServiceTicket *ticket, id object, NSError *error) {
        if (error) {
          [ViewHelper showPopup:@"Error"
                        message:@"Unable to check in, try again."
                         button:@"OK"];
          NSLog(@"Error while checking in: %@", error);
          // Reauthenticate the user
          [self authenticateUser];

        } else {
          // Get a list of offers if it's not pulled before
          if (!nextController.offers) {
            [self fetchOffersForPlaceID:place.placeId nextController:nextController];
          }

          // Get a list of recommendations if it's not pulled before
          if (!nextController.recommendations) {
            [self fetchRecommendationsForPlaceID:place.placeId nextController:nextController];
          }
        }
      }];
}

#pragma mark - Offer Model

- (void)fetchOffersForPlaceID:(NSNumber *)placeID
      nextController:(OffersTableViewController *)nextController {
  GTLQueryShoppingAssistant *query =
      [GTLQueryShoppingAssistant queryForOffersListOffers];

  [self.service executeQuery:query
           completionHandler:^(GTLServiceTicket *ticket,
                               GTLShoppingAssistantOfferCollection *object,
                               NSError *error) {
             if (error) {
               [ViewHelper showPopup:@"Error"
                             message:@"Retrieving offers failed."
                              button:@"OK"];
               NSLog(@"Error while fetching offers: %@", error);
             } else {
               nextController.offers = [object items];
             }
           }];
}

#pragma mark - Recommendation Model

- (void)fetchRecommendationsForPlaceID:(NSNumber *)placeID
               nextController:(OffersTableViewController *)nextController {
  GTLQueryShoppingAssistant *query = [GTLQueryShoppingAssistant
      queryForRecommendationsListRecommendationsWithPlaceId:placeID];

  [self.service executeQuery:query
      completionHandler:^(GTLServiceTicket *ticket,
                          GTLShoppingAssistantRecommendationCollection *object,
                          NSError *error) {
        if (error) {
          [ViewHelper showPopup:@"Error"
                        message:@"Retrieving recommendations failed."
                         button:@"OK"];
          NSLog(@"Error while fetching recommendations: %@", error);
        } else {
          nextController.recommendations = [object items];

        }
      }];
}

@end

import requests
import time

class OGS:
    @staticmethod
    def pp(json_data):
        """ Pretty-print JSON data """
        import json
        formatted_json = json.dumps(json_data, indent=4)
        print("pp:", formatted_json)

    @staticmethod
    def get_json(url):
        """ Fetch JSON data from the given URL """
        try:
            response = requests.get(url)
            if response.status_code == 429:
                print("429! - Going to sleep.")
                time.sleep(5)
                print("429! - Awake.")
                return None

            response.raise_for_status()
            time.sleep(0.2)  # Simulate delay in request handling
            return response.json()
        except requests.exceptions.RequestException as e:
            print("Caught:", e)
            return None

    @staticmethod
    def list_reviews(player_id):
        """ List reviews for a given player ID """
        games = OGS.get_json(f"https://online-go.com/api/v1/players/{player_id}/games")

        if games is None:
            print("Failed to retrieve games data.")
            return

        print(f"{games.get('count', 'Unknown')} games.")
        next_page = games.get("next")

        while next_page:
            for game in games.get("results", []):
                game_id = game.get("id")
                started = game.get("started")
                ended = game.get("ended")
                reviews_url = f"https://online-go.com/api/v1/games/{game_id}/reviews"

                reviews = OGS.get_json(reviews_url)

                if reviews is None:
                    print("Reviews is null! Probably due to a 429 response, stopping.")
                    return

                review_count = reviews.get("count", 0)
                if review_count == 0:
                    continue

                print(f"Game: {game_id} {game.get('name')} {started} - {ended}")

                for review in reviews.get("results", []):
                    username = review.get("owner", {}).get("username")
                    print(f"\tReview by: {username}")

            games = OGS.get_json(next_page)
            next_page = games.get("next") if games else None

    @staticmethod
    def main(args=None):
        """ Main function to list reviews for players """
        ray, hugh = 179, 1567393
        player_id = hugh if not args else int(args[0])
        OGS.list_reviews(player_id)

# Example usage
if __name__ == "__main__":
    OGS.main()